package io.hypergroup.hyper;

import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import bolts.Continuation;
import bolts.Task;
import io.hypergroup.hyper.context.HyperContext;
import io.hypergroup.hyper.context.cache.HyperCache;
import io.hypergroup.hyper.exception.DataParseException;
import io.hypergroup.hyper.exception.IndexErrorException;
import io.hypergroup.hyper.exception.InvalidCollectionException;
import io.hypergroup.hyper.exception.MissingPropertyException;
import io.hypergroup.hyper.exception.NoHrefException;
import io.hypergroup.hyper.exception.WrongDatatypeException;
import io.hypergroup.hyper.context.requests.ConcurrentRequestPool;
import io.hypergroup.hyper.context.requests.ResponsePackage;

/**
 * Hyper node.
 * <br/>
 * Hyper.createRoot("https://api.example.com/v1/root").get("current_user.projects.100.name") => "Hyper"
 *
 * @author http://hypergroup.github.io/
 * @author https://github.com/explodes
 */
public abstract class Hyper {

    /**
     * Tag for logging.
     */
    private static final String TAG = Hyper.class.getSimpleName();

    /**
     * Friendly name for the key path for root
     */
    private static final String KEY_PATH_ROOT = "root";

    /**
     * Href of this object. Used for fetching
     */
    protected String mHref;

    /**
     * Whether or not data has been fetched
     */
    private boolean mFetched = false;

    /**
     * Internal underlying data
     */
    private Data mData;

    /**
     * Key path of this Hyper node
     */
    private String mKeyPath;

    /**
     * Context this hyper node is in, includes root, http clients, and caching.
     */
    private HyperContext mContext;

    /**
     * Construct a Hyper node for a given url, un-fetched.
     *
     * @param href    URL to act as the href of this node
     * @param context Context of this hyper node
     */
    public Hyper(String keyPath, String href, HyperContext context) {
        mKeyPath = keyPath;
        mHref = href;
        mContext = context;
        save();
    }

    /**
     * Construct a Hyper node with a given Data package.
     *
     * @param data    Initial data. More can be fetched if the data provided has an href
     * @param context Context of this hyper node
     */
    public Hyper(String keyPath, Data data, HyperContext context) {
        mKeyPath = keyPath;
        try {
            mHref = data.getHref();
        } catch (NoHrefException e) {
            mHref = null;
        }
        setData(data);
        mContext = context;
        save();
    }

    /**
     * Get a value for a key path.
     * <p/>
     * example key paths:
     * <br/>
     * current_user
     * <br/>
     * current_user.company
     * <br/>
     * current_user.avatar.default.url
     * <br/>
     * current_user.friends.0.avatar.default.url
     * <br/>
     *
     * @param keyPath keyPath to search through this node.
     * @param <T>     Type of Object that you are expecting to get
     * @return A Task for the type of object you are expecting to get. Typical types are Hyper,
     * String, and boxed primitives.
     * <br/>
     * You could receive special error conditions from Hyper.
     * <br/>
     * HyperException
     * <br/>
     * NoHrefException
     * <br/>
     * MissingPropertyException
     * <br/>
     * IndexErrorException
     * <br/>
     * InvalidCollectionException
     * <br/>
     * WrongDatatypeException
     */
    public <T> Task<T> get(final String keyPath) {

        // Extract key information
        final KeyPath parsed = new KeyPath(keyPath);

        // ## Cache

        // check for a cached version
        Task<T> cachedTask = getCachedTask(keyPath);
        // if the task is there,
        if (cachedTask != null) {
            // we have a cached object.
            // return the task that is wrapping it
            return cachedTask;
        }

        // ## Fetch

        // Ensure that our results are fetched
        Task<Data> fetchTask;
        if (shouldFetchKey(parsed.nodeKey)) {
            // perform the actual fetch in a task
            fetchTask = getFetchTask();
        } else {
            // empty fetch task, pretend it is done
            fetchTask = Task.forResult(null);
        }

        // after fetching is complete, we are ready to get our property out of the underlying data
        return fetchTask.continueWithTask(new Continuation<Data, Task<T>>() {
            @Override
            public Task<T> then(Task<Data> task) throws Exception {
                // if there was an error with fetching
                if (task.isFaulted()) {
                    // pass on the fetch error
                    return Task.forError(task.getError());
                } else {
                    // if fetching went ok
                    // return a property task
                    if (parsed.isMultikey()) {
                        // complex path
                        return getDeepPropertyTask(parsed.nodeKey, parsed.nextKey);
                    } else {
                        // single key
                        // path down the new key path for this object
                        String keyPath = getConcatenatedKeyPath(parsed.nodeKey);
                        // create the task
                        return getPropertyTask(keyPath, parsed.nodeKey);
                    }
                }
            }
        });
    }

    /**
     * Retrieve each item in this Hyper node's collection
     *
     * @param <T> Type of Objects that you are expecting to get
     * @return A Task with a list of objects
     */
    public <T> Task<List<T>> each() {
        // Un-refactored and dirty.

        // Our results
        final Task<List<T>>.TaskCompletionSource result = Task.create();

        // ## Fetch

        // Ensure that our results are fetched
        Task<Data> fetchTask;
        if (!isFetched()) {
            // perform the actual fetch in a task
            fetchTask = getFetchTask();
        } else {
            // empty fetch task, pretend it is done
            fetchTask = Task.forResult(null);
        }

        fetchTask.continueWith(new Continuation<Data, Void>() {
            @Override
            public Void then(Task<Data> task) throws Exception {
                if (task.isFaulted()) {
                    // save error state
                    result.setError(task.getError());
                } else {

                    // ## Collection

                    // acquire the collection or error out and exit
                    List<Object> collection;
                    try {
                        // try to get the collection
                        collection = getCollection();
                    } catch (InvalidCollectionException ex) {
                        // save the error state
                        result.setError(ex);
                        // exit
                        return null;
                    }

                    // ## Each Item

                    // prepare to build the list
                    // flag to not save results if we had an error
                    boolean err = false;
                    // size of our collection
                    int N = collection.size();
                    // list to save entries to
                    final List<T> items = new ArrayList<T>(N);
                    for (int index = 0; index < N; index++) {

                        // ## Item

                        // get the value from the collection
                        Object value = collection.get(index);
                        // if its null
                        if (value == null) {
                            // save a null
                            items.add(null);
                        } else {

                            // ## Cache

                            // Build the key
                            String nodeKey = String.valueOf(index);
                            // check for a cached version
                            Object cachedValue = getCachedNode(nodeKey);
                            // if the item exists naturally, and we have a cached version,
                            if (cachedValue != null) {
                                // use the cached version
                                value = cachedValue;
                            }

                            // ## Hyper node

                            // if it is Data, save a Hyper
                            if (value instanceof Data) {
                                // path down the new key path for this object
                                String keyPath = getConcatenatedKeyPath(nodeKey);
                                // create the new node
                                value = createHyperNodeFromData(keyPath, (Data) value);
                            }

                            // ## Coerce

                            // coerce to the expected else fail
                            try {
                                items.add((T) value);
                            } catch (ClassCastException ex) {
                                // save error state and stop
                                result.setError(new WrongDatatypeException(ex));
                                err = true;
                                break;
                            }
                        }
                    }
                    // if we didn't have an error, save the results
                    if (!err) {
                        result.setResult(items);
                    }
                }
                return null;
            }
        });


        return result.getTask();
    }

    /**
     * Attempt to retrieve a Hyper node from the cache, if applicable
     *
     * @param keyPath Relative node path to retrieve
     * @param <T>     Type of Object that you are expecting to get (Hyper)
     * @return A task or null
     */
    protected <T> Task<T> getCachedTask(String keyPath) {
        // get the node from cache
        Hyper node = getCachedNode(keyPath);
        // if we have a node
        if (node != null) {
            // try to return a task for it
            try {
                // return success
                return Task.forResult((T) node);
            } catch (ClassCastException ex) {
                // return error state
                return Task.forError(new WrongDatatypeException(ex));
            }
        }
        // return nothing!
        return null;
    }

    /**
     * Get a node from cache by relative keyPath
     *
     * @param keyPath Relative key path to get from the cache
     * @return The Hyper node if found, otherwise null
     */
    protected Hyper getCachedNode(String keyPath) {
        // get the cache
        HyperCache cache = getContext().getHyperCache();
        // if we don't have a cache
        if (cache == null) {
            // exit, we can't do anything
            return null;
        }

        // start with the whole key-path
        String subpath = getConcatenatedKeyPath(keyPath);

        // find the end-most cached item
        while (true) {
            // get a node at that path
            Hyper node = cache.get(subpath);
            // if it exits
            if (node != null) {
                return node;
            }
            // get the next subpath or abort
            int lastIndexOfDot = subpath.lastIndexOf('.');
            // if no more dots
            if (lastIndexOfDot == -1) {
                // abort!
                return null;
            }
            // trim off last key
            subpath = subpath.substring(0, lastIndexOfDot);
        }
    }

    /**
     * Create a task will fetch properties of a sub-node.
     * This function serves as the main recurvise component for the traversal algorithm.
     * It acquires a hyper-node with "nodeKey" and then continues to traverse down the remaining
     * keyPath but using the "get" function that originally created this task.
     *
     * @param nodeKey    The key of the sub-node to extract properties from
     * @param newKeyPath The sub-node's keyPath to extract properties from
     * @param <T>        Type of Object that you are expecting to get
     */
    protected <T> Task<T> getDeepPropertyTask(String nodeKey, final String newKeyPath) {
        // key path
        // get the underlying node
        return get(nodeKey).continueWithTask(new Continuation<Object, Task<T>>() {
            @Override
            public Task<T> then(Task<Object> task) throws Exception {
                // if getting the node fell apart
                if (task.isFaulted()) {
                    // pass on the error
                    return Task.forError(task.getError());
                } else {
                    // otherwise use that new node to get the next parts in the key path
                    return ((Hyper) task.getResult()).get(newKeyPath);
                }
            }
        });
    }


    /**
     * Retrieve a property that exists directly on this Hyper node.
     *
     * @param key Key of the property to get
     * @param <T> Type of Object that you are expecting to get
     * @return A Task for the type of object you are expecting to get. See {@link #get} for more
     * information about the Task.
     */
    protected <T> Task<T> getPropertyTask(String keyPath, String key) {
        Task<T>.TaskCompletionSource result = Task.create();
        getFetchedProperty(keyPath, result, key);
        return result.getTask();
    }

    /**
     * Fetch a property from the underlying data source.
     *
     * @param result Result resource to which errors and success are saved to
     * @param key    Key of the property to retrieve
     * @param <T>    Type of Object that you are expecting to get
     */
    protected <T> void getFetchedProperty(String keyPath, Task<T>.TaskCompletionSource result, String key) {
        // if we are dealing a numeric key
        Integer index = asIndex(key);
        if (index != null) {
            // get the item from the collection at the given index
            getItemFromCollection(keyPath, result, index);
        } else {
            // otherwise we are dealing with a property at a given key
            getPropertyFromData(keyPath, result, key);
        }
    }

    /**
     * Retrieve a property directly from the underlying Data
     *
     * @param result Result resource to which errors and success are saved to
     * @param key    Key of the property to retrieve
     * @param <T>    Type of Object that you are expecting to get
     */
    protected <T> void getPropertyFromData(String keyPath, Task<T>.TaskCompletionSource result, String key) {
        Object value;
        try {
            // attempt to extract the key
            value = getData().getProperty(key);
        } catch (MissingPropertyException e) {
            // no mapping found, save an error and exit
            result.setError(e);
            return;
        }
        // turn our value into something meaningful and save it to the results
        hyperify(keyPath, result, value);
    }

    /**
     * Retrieve the underlying collection of the Hyper node
     *
     * @return The collection.
     */
    protected List<Object> getCollection() throws InvalidCollectionException {
        return getData().getCollection();
    }

    /**
     * Retrieve a property directly from the underlying "collection" Data
     *
     * @param result Result resource to which errors and success are saved to
     * @param index  Index of the item you are trying to get
     * @param <T>    Type of Object that you are expecting to get
     */
    protected <T> void getItemFromCollection(String keyPath, Task<T>.TaskCompletionSource result, int index) {

        // ## Collection

        // acquire the collection or error out and exit
        List<Object> collection;
        try {
            // try to get the collection
            collection = getCollection();
        } catch (InvalidCollectionException ex) {
            // save the error state
            result.setError(ex);
            // exit
            return;
        }

        // ## Item

        // attempt to extract the element at the given index
        Object value;
        try {
            value = collection.get(index);

        } catch (IndexOutOfBoundsException e) {
            // no mapping found, save an error and exit
            result.setError(new IndexErrorException("Invalid index", e));
            return;
        }

        // ## Cache

        // if an element exists at that location
        if (value != null) {
            // Build the key
            String nodeKey = String.valueOf(index);
            // check for a cached version
            Object cachedValue = getCachedNode(nodeKey);
            // if the item exists naturally, and we have a cached version,
            if (cachedValue != null) {
                // prefer the cached version
                value = cachedValue;
            }
        }

        // ## Clean

        // turn our value into something meaningful and save it to the results
        hyperify(keyPath, result, value);
    }


    /**
     * Perform a fetch if an href is available, otherwise, set error state.
     *
     * @return A Task who's completion indicates that there was either an error fetching or that
     * fetching is complete.
     */
    protected Task<Data> getFetchTask() {
        String href = getHref();
        if (TextUtils.isEmpty(href)) {
            // no href to fetch for, return error state
            return Task.forError(new NoHrefException("Attempting to fetch data without an \"href\""));
        } else {
            // otherwise do the actual fetch in a task
            return getNetworkFetchTask();
        }
    }

    /**
     * Create an Async task that gets Data from the internet.
     *
     * @return A Task who's completion indicates that there was either an error fetching or that
     * fetching is complete.
     */
    protected Task<Data> getNetworkFetchTask() {
        // We'll need a few context variables so lets get the context
        HyperContext context = getContext();
        // get the client
        OkHttpClient client = context.getHttpClient();
        // and the executor
        Executor executor = context.getNetworkExecutor();
        // and the request pool
        ConcurrentRequestPool pool = context.getConcurrentRequestPool();
        // and our href
        String href = getHref();
        // build a request to the href
        Request request = buildRequest(href);
        // using our pool, make a request, and then use the response to build Data
        return pool.getResponseTask(client, executor, request).continueWithTask(new Continuation<ResponsePackage, Task<Data>>() {
            @Override
            public Task<Data> then(Task<ResponsePackage> task) throws Exception {
                // Create our deferred
                Task<Data>.TaskCompletionSource result = Task.create();
                // if the request error'd out
                if (task.isFaulted()) {
                    // save error state
                    result.setError(task.getError());
                } else {
                    // Use the response
                    ResponsePackage response = task.getResult();
                    // under all circumstances, we have fetched, but we have only fetched when the
                    // following block is completed or fails to complete
                    try {
                        // parse data from that
                        Data data = parseResponse(response);
                        // save the data
                        setData(data);
                        // save success state
                        result.setResult(data);
                    } catch (Exception ex) {
                        // save error state
                        result.setError(ex);
                    } finally {
                        // mark fetched as having occurred whether or not the process succeeds
                        setFetched(true);
                    }
                }
                return result.getTask();
            }
        });
    }

    /**
     * Whether or not we should do a live fetch to acquire a key
     *
     * @param key Key to test whether or not a fetch is required for
     */

    protected boolean shouldFetchKey(String key) {
        // if we've already fetched
        if (isFetched()) {
            // we do not need to fetch
            return false;
        }
        // if we have json that has the key
        Data data = getData();
        if (data != null && data.hasProperty(key)) {
            // we don't need to fetch
            return false;
        }
        // we need to fetch in all other circumstances
        return true;

    }

    /**
     * Convert JSONObjects to Hyper nodes and test that the value of an object is the same type that
     * we are expecting to get.
     *
     * @param result Result resource to which errors and success are saved to
     * @param value  Fetched value to coerce as an expected type
     * @param <T>    Type of Object that you are expecting to get
     * @throws WrongDatatypeException When T does not match up with the type of `value`
     */
    protected <T> void hyperify(String keyPath, Task<T>.TaskCompletionSource result, Object value) {
        if (value == null) {
            // null object null result, save and exit
            result.setResult(null);
        } else {
            // turn JSONObjects into Hyper nodes
            if (isRawData(value)) {
                // turn that raw data into something meaningful
                Data data = createDataFromRawData(value);
                // put that data in a meaningful hyper node
                value = createHyperNodeFromData(keyPath, data);
            } else if (value instanceof Data) {
                // put that data in a meaningful hyper node
                value = createHyperNodeFromData(keyPath, (Data) value);
            }
            // The TRUE VALUE of value, as the expected type
            T trueValue;
            try {
                // cast the value as the expected type
                trueValue = (T) value;
            } catch (ClassCastException ex) {
                // If the cast failed, then we got a datatype different than the one we expected.
                // save the error state and exit
                Log.w(TAG, "WrongDatatypeException", ex);
                result.setError(new WrongDatatypeException(ex));
                return;
            }
            // trueValue is what we expected, save success state
            result.setResult(trueValue);
        }
    }

    /**
     * Attempt to coerce a key as an index.
     *
     * @param key Key to coerce
     * @return An Integer if the key is a number or null if the key is not
     */
    private Integer asIndex(String key) {
        try {
            // success
            return Integer.parseInt(key, 10);
        } catch (NumberFormatException ex) {
            // failure
            return null;
        }
    }

    /**
     * @return The context of this Hyper node
     */
    protected HyperContext getContext() {
        return mContext;
    }

    /**
     * Save this Hyper node in the cache, if applicable
     */
    protected void save() {
        HyperCache cache = getContext().getHyperCache();
        if (cache != null) {
            cache.save(this);
        }
    }

    /**
     * Retrieve the underlying data source for this Hyper node
     */
    protected Data getData() {
        return mData;
    }

    /**
     * Set the underlying data source for this Hyper node
     */
    protected void setData(Data data) {
        mData = data;
    }

    /**
     * @return Return the key path of this Hyper node, or null if it is the root node.
     */
    public String getKeyPath() {
        return mKeyPath;
    }

    /**
     * @return Return the key path of this Hyper node, or "root" if it is the root node.
     */
    public String getFriendlyKeyPath() {
        String keyPath = getKeyPath();
        if (keyPath == null) {
            return KEY_PATH_ROOT;
        } else {
            return keyPath;
        }
    }

    /**
     * @return Return the href this Hyper node sits at
     */
    public String getHref() {
        return mHref;
    }

    /**
     * @return Return the fetched state of this Hyper node
     */
    public boolean isFetched() {
        return mFetched;
    }

    /**
     * Set whether or not the underlying data has been fetched
     */
    protected void setFetched(boolean fetched) {
        mFetched = fetched;
    }

    /**
     * Clears *all* data in this node and removes this node from the cache, if applicable
     */
    public void invalidate() {
        setData(null);
        setFetched(false);
        HyperCache cache = getContext().getHyperCache();
        if (cache != null) {
            cache.invalidate(this);
        }
    }

    /**
     * Invalidate a keyPath from the cache, if applicable
     *
     * @param keyPath Key path to invalidate
     */
    public void invalidate(String keyPath) {
        HyperCache cache = getContext().getHyperCache();
        if (cache != null) {
            cache.invalidate(keyPath);
        }
    }

    /**
     * Return the root node of this Hyper node
     */
    public Hyper getRoot() {
        return getContext().getRoot();
    }

    /**
     * Concatenate key path elements
     *
     * @param key Key to append to the current path
     * @return A concatenation of the two parts
     */
    public String getConcatenatedKeyPath(String key) {
        return KeyPath.concat(getKeyPath(), key);
    }

//    @Override
//    public String toString() {
//        return "(" + getFriendlyKeyPath() + ") @ " + getHref();
//    }

    /**
     * Build a request for retrieving an href.
     * <br/>
     * Take this opportunity to set appropriate request headers.
     *
     * @param href URL to retrieve
     * @return The request that will be used to pull data from the remote server.
     */
    protected abstract Request buildRequest(String href);

    /**
     * Parse a response into Data
     *
     * @param response Response to parse
     * @return The wrapped Data
     * @throws DataParseException When parsing goes wrong
     * @throws IOException        When IO goes wrong
     */
    protected abstract Data parseResponse(ResponsePackage response) throws IOException, DataParseException;

    /**
     * Return whether or not the data is in the raw underlying format (for example, JSONObject)
     *
     * @param value Value found as a property of a Hyper node
     * @return Whether or not to transform the object
     */
    protected abstract boolean isRawData(Object value);

    /**
     * Construct a new Hyper node with the given data
     *
     * @param keyPath Key path of the new node
     * @param data    Base data for the new node
     * @return The new Hyper node
     */
    protected abstract Hyper createHyperNodeFromData(String keyPath, Data data);

    /**
     * Create Data from raw data (for example, JSONObject)
     *
     * @param raw Raw data for the new Data
     * @return The new Data
     */
    protected abstract Data createDataFromRawData(Object raw);

    /**
     * Class that encapsulates common keyPath parsing.
     */
    /* default */ static class KeyPath {
        String nodeKey;
        String nextKey;

        /**
         * Construct this key path, searching for the local node and extracting the remote nodes.
         * <br/>
         * No validation is performed on the string.
         *
         * @param keyPath Key path to parse
         */
        KeyPath(String keyPath) {
            // if a Key path was provided
            if (!TextUtils.isEmpty(keyPath)) {
                // if it has multiple parts
                if (keyPath.contains(".")) {
                    // break the path apart
                    String[] parts = keyPath.split("\\.", 2);
                    // save the first part
                    nodeKey = parts[0];
                    // and the remainder
                    nextKey = parts[1];
                } else {
                    // otherwise we have just a simple path
                    nodeKey = keyPath;
                    // with no remainder
                    nextKey = null;
                }
            }
        }

        /**
         * @return Whether or not this key has multiple levels, i.e "foo.bar" versus just "foo"
         */
        /* default */ boolean isMultikey() {
            return nextKey != null;
        }

        /**
         * Concatenate two paths
         *
         * @param baseKey First part of the path
         * @param keyPath Second part of the path
         * @return "."-joined coalesced strings
         */
        public static String concat(String baseKey, String keyPath) {
            if (TextUtils.isEmpty(baseKey)) {
                return keyPath;
            } else if (TextUtils.isEmpty(keyPath)) {
                return baseKey;
            } else {
                return baseKey + "." + keyPath;
            }
        }
    }

}
