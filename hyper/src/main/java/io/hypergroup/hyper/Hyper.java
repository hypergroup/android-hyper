package io.hypergroup.hyper;

import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import bolts.Continuation;
import bolts.Task;
import io.hypergroup.hyper.context.HyperContext;
import io.hypergroup.hyper.context.cache.HyperCache;
import io.hypergroup.hyper.context.requests.ConcurrentRequestPool;
import io.hypergroup.hyper.context.requests.ResponsePackage;
import io.hypergroup.hyper.exception.DataParseException;
import io.hypergroup.hyper.exception.IndexErrorException;
import io.hypergroup.hyper.exception.InvalidCollectionException;
import io.hypergroup.hyper.exception.MissingPropertyException;
import io.hypergroup.hyper.exception.NoHrefException;
import io.hypergroup.hyper.exception.WrongDataTypeException;

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
    protected URL mHref;

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
    public Hyper(String keyPath, URL href, HyperContext context) {
        mKeyPath = keyPath;
        mHref = href;
        mContext = context;
        saveToCache();
    }

    /**
     * Construct a Hyper node with a given Data package.
     *
     * @param data    Initial data. More can be fetched if the data provided has an href
     * @param context Context of this hyper node
     */
    public Hyper(String keyPath, URL relativeHref, Data data, HyperContext context) {
        mKeyPath = keyPath;
        try {
            mHref = data.getHref(relativeHref);
        } catch (NoHrefException e) {
            mHref = null;
        }
        setData(data);
        mContext = context;
        saveToCache();
    }

    /**
     * Get a keyPath property in the form of a dot-notation.
     * <br/>
     * ex.
     * <pre>
     *     current_user.first_name
     *     current_user.display_name
     *     current_user.friends
     *     current_user.friends.0
     *     current_user.friends.0.first_name
     *     current_user.friends.0.display_name
     *     current_user.friends.0.groups.0.name
     * </pre>
     *
     * @param keyPath Key path to retrieve
     * @param <T>     Expected type to retrieve
     * @return The fetched type
     * @throws InterruptedException       Network fetch was interrupted
     * @throws NoHrefException            When fetching deep-links, if there is a missing link between nodes, this error will occur
     * @throws IndexErrorException        When fetching a specific index from a collection but that index does not exist
     * @throws MissingPropertyException   When fetching a property that doesn't exist
     * @throws InvalidCollectionException When fetching from a collection that doesn't exist or in an unexpected format
     * @throws WrongDataTypeException     When fetching an object that doesn't have the same class as the specified type T
     * @see #getAsync(String)
     */
    public <T> T get(final String keyPath) throws InterruptedException, NoHrefException, IndexErrorException, MissingPropertyException, InvalidCollectionException, WrongDataTypeException {

        // ## Parse Key
        final KeyPath parsed = new KeyPath(keyPath);

        // ## If we already have it
        if (!shouldFetchForKey(parsed.nodeKey)) {
            return getProperty(parsed);
        }

        // ## Cache
        Hyper cached = getImmediateCachedNode(parsed.nodeKey);
        if (cached != null) {
            if (parsed.isMultiKey()) {
                return cached.get(parsed.nextKey);
            }
        }

        // ## Fetch
        if (shouldFetchForKey(parsed.nodeKey)) {
            performNetworkFetch();
        }

        return getProperty(parsed);
    }


    /**
     * Retrieve each item in this Hyper node's collection
     * <br/>
     * <pre>
     *     links.&lt;Object&gt;each()
     *     users.&lt;Hyper&gt;each()
     *     scores.&lt;Integer&gt;each()
     * </pre>
     *
     * @param <T> Type of Objects that you are expecting to get
     * @return The list of objects from the given collection in the given format
     * @throws InterruptedException       Network fetch was interrupted
     * @throws NoHrefException            When fetching deep-links, if there is a missing link between nodes, this error will occur
     * @throws InvalidCollectionException When fetching from a collection that doesn't exist or in an unexpected format
     * @throws WrongDataTypeException     When fetching an object that doesn't have the same class as the specified type T
     * @see #eachAsync(boolean)
     */
    public <T> List<T> each() throws InterruptedException, NoHrefException, InvalidCollectionException, WrongDataTypeException {
        // ## Fetch

        // Ensure that our results are fetched
        if (!isFetched()) {
            performNetworkFetch();
        }

        // ## Use our collection
        List<Object> collection = getCollection();

        // ## Each Item

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
                String relativePath = String.valueOf(index);

                // check for a cached version
                Object cachedValue = getImmediateCachedNode(relativePath);
                // if the item exists naturally, and we have a cached version,
                if (cachedValue != null) {
                    // use the cached version
                    value = cachedValue;
                }

                // ## Coerce
                value = coerce(relativePath, value);

                // coerce to the expected else fail
                try {
                    items.add((T) value);
                } catch (ClassCastException ex) {
                    // save error state and stop
                    throw new WrongDataTypeException(ex);
                }
            }
        }
        // if we didn't have an error, save the results
        return items;
    }

    /**
     * Retrieve an individual property, recursively acquiring the property as needed
     *
     * @param keyPath Key path to retrieve
     * @param <T>     Expected type to retrieve
     * @return The value of the property retrieved at the given key path
     * @throws InterruptedException       Network fetch was interrupted
     * @throws NoHrefException            When fetching deep-links, if there is a missing link between nodes, this error will occur
     * @throws IndexErrorException        When fetching a specific index from a collection but that index does not exist
     * @throws MissingPropertyException   When fetching a property that doesn't exist
     * @throws InvalidCollectionException When fetching from a collection that doesn't exist or in an unexpected format
     * @throws WrongDataTypeException     When fetching an object that doesn't have the same class as the specified type T
     */
    protected <T> T getProperty(KeyPath keyPath) throws NoHrefException, InterruptedException, WrongDataTypeException, MissingPropertyException, IndexErrorException, InvalidCollectionException {
        if (keyPath.isMultiKey()) {
            return ((Hyper) get(keyPath.nodeKey)).get(keyPath.nextKey);
        } else {
            // if we are dealing a numeric nodeKey
            Integer index = asIndex(keyPath.nodeKey);
            if (index != null) {
                // get the item from the collection at the given index
                return getItemFromCollection(keyPath, index);
            } else {
                // otherwise we are dealing with a property at a given nodeKey
                return getPropertyFromData(keyPath);
            }
        }
    }

    /**
     * Retrieve a property directly from underlying data
     *
     * @param keyPath Key path to retrieve
     * @param <T>     Expected type to retrieve
     * @return Retrieved property
     * @throws MissingPropertyException When fetching a property that doesn't exist
     * @throws WrongDataTypeException   When fetching an object that doesn't have the same class as the specified type T
     */
    protected <T> T getPropertyFromData(KeyPath keyPath) throws MissingPropertyException, WrongDataTypeException {
        Object value;
        // attempt to retrieved cached node so that we do not have to create another
        Hyper cached = getImmediateCachedNode(keyPath.nodeKey);
        if (cached != null) {
            value = cached;
        } else {
            value = getData().getProperty(keyPath.nodeKey);
        }
        // turn our value into something meaningful and save it to the results
        return coerce(keyPath.relativePath, value);
    }

    /**
     * Retrieve the underlying collection of the Hyper node
     *
     * @return A list of objects from the underlying collection
     * @throws InvalidCollectionException When fetching from a collection that doesn't exist or in an unexpected format
     */
    protected List<Object> getCollection() throws InvalidCollectionException {
        return getData().getCollection();
    }

    /**
     * Retrieve a property directly from the underlying "collection" Data
     *
     * @param keyPath Base key path for lazy loaded items
     * @param index   Index path of the item to retrieve
     * @param <T>     Expected return type
     * @return The value of the item at the given index
     * @throws IndexErrorException        When fetching a specific index from a collection but that index does not exist
     * @throws InvalidCollectionException When fetching from a collection that doesn't exist or in an unexpected format
     * @throws WrongDataTypeException     When fetching an object that doesn't have the same class as the specified type T
     */
    protected <T> T getItemFromCollection(KeyPath keyPath, int index) throws InvalidCollectionException, IndexErrorException, WrongDataTypeException {

        // ## Collection

        // acquire the collection or error out and exit
        List<Object> collection = getCollection();

        // ## Item

        // attempt to extract the element at the given index
        Object value;
        try {
            value = collection.get(index);

        } catch (IndexOutOfBoundsException e) {
            // no mapping found, save an error and exit
            throw new IndexErrorException("Invalid index", e);
        }

        // ## Cache

        // if an element exists at that location
        if (value != null) {
            Object cachedValue = getImmediateCachedNode(String.valueOf(index));
            // if the item exists naturally, and we have a cached version,
            if (cachedValue != null) {
                // prefer the cached version
                value = cachedValue;
            }
        }

        // ## Clean

        // turn our value into something meaningful and save it to the results
        return coerce(keyPath.relativePath, value);
    }

    /**
     * @param node Property of this object to get from cache
     * @return Hyper node from cache, or null
     */
    protected Hyper getImmediateCachedNode(String node) {
        return getExactCachedNode(getConcatenatedKeyPath(node));

    }

    /**
     * Get a Hyper node from the cache at the exact given path
     *
     * @param fullPath exact path to pull from cache
     * @return Hyper node from cache, or null
     */
    protected Hyper getExactCachedNode(String fullPath) {
        // get the cache
        HyperCache cache = getContext().getHyperCache();
        // abandon all hope if we don't have a cache
        if (cache == null) {
            return null;
        }
        return cache.get(fullPath);
    }

    /**
     * Perform a fetch of the node's data if an href is available, otherwise, set error state.
     *
     * @return This object's fetched and parsed Data
     * @throws InterruptedException Network fetch was interrupted
     * @throws NoHrefException      When fetching deep-links, if there is a missing link between nodes, this error will occur
     */
    protected Data performNetworkFetch() throws NoHrefException, InterruptedException {
        URL href = getHref();
        if (href == null) {
            // no href to fetch for, return error state
            throw new NoHrefException("Attempting to fetch data without an \"href\"");
        } else {
            // otherwise do the actual fetch in a task
            return getNetworkFetchTask();
        }
    }

    /**
     * Fetch and parse data from the internet using the context's network executor.
     * <br/>
     * Requests are pooled in a temporary cache to prevent duplicate network requests.
     *
     * @return Parsed data
     * @throws InterruptedException Network fetch was interrupted
     */
    protected Data getNetworkFetchTask() throws InterruptedException {
        // We'll need a few context variables so lets get the context
        HyperContext context = getContext();
        // get the client
        OkHttpClient client = context.getHttpClient();
        // and the executor
        Executor executor = context.getNetworkExecutor();
        // and the request pool
        ConcurrentRequestPool pool = context.getConcurrentRequestPool();
        // and our href
        URL href = getHref();
        // build a request to the href
        Request request = buildRequest(href);
        // using our pool, make a request, and then use the response to build Data
        Task<Data> dataTask = pool.getResponseTask(client, executor, request).continueWithTask(new Continuation<ResponsePackage, Task<Data>>() {
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
        dataTask.waitForCompletion();
        if (dataTask.isFaulted()) {
            Log.e(TAG, "Error fetching data", dataTask.getError());
        }
        return dataTask.getResult();
    }

    /**
     * Whether or not we should do a live fetch to acquire a key
     *
     * @param key Key to test whether or not a fetch is required for
     */

    protected boolean shouldFetchForKey(String key) {
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
     * Convert Objects to Hyper nodes or to the type of objects we are expecting to get
     *
     * @param relativePath Relative path for new Hyper nodes
     * @param value        Value to coerce
     * @param <T>          Type to coerce value into
     * @return Coerced value
     * @throws WrongDataTypeException When fetching an object that doesn't have the same class as the specified type T
     */
    protected <T> T coerce(String relativePath, Object value) throws WrongDataTypeException {
        if (value == null) {
            // null object null result, save and exit
            return null;
        } else {
            // turn JSONObjects into Hyper nodes
            if (isRawData(value)) {
                // turn that raw data into something meaningful
                Data data = createDataFromRawData(value);
                // put that data in a meaningful hyper node
                value = createHyperNodeFromData(relativePath, mHref, data);
            } else if (value instanceof Data) {
                // put that data in a meaningful hyper node
                value = createHyperNodeFromData(relativePath, mHref, (Data) value);
            }
            // The TRUE VALUE of value, as the expected type
            T trueValue;
            try {
                // cast the value as the expected type
                trueValue = (T) value;
            } catch (ClassCastException ex) {
                // If the cast failed, then we got a datatype different than the one we expected.
                // save the error state and exit
                Log.w(TAG, "WrongDataTypeException", ex);
                throw new WrongDataTypeException(ex);
            }
            // trueValue is what we expected, save success state
            return trueValue;
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
    protected void saveToCache() {
        HyperCache cache = getContext().getHyperCache();
        if (cache != null) {
            cache.save(this);
        }
    }

    /**
     * Retrieve the underlying data source for this Hyper node
     */
    public Data getData() {
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
            return KeyPath.concat(KEY_PATH_ROOT, keyPath);
        }
    }

    /**
     * @return Return the href this Hyper node sits at
     */
    public URL getHref() {
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

    /**
     * Get the keys available on this Hyper node.
     *
     * @return An array of String keys or null if there is no data.
     */
    public String[] getAvailableKeys() {
        Data data = getData();
        if (data != null) {
            return data.getKeys();
        }
        return null;
    }

    /**
     * Fetch this Hyper node's data. Does not make a network request if this Hyper node has already
     * been fetched.
     *
     * @return A Task that may have either an error or a result, which is this instance.
     */
    public Hyper fetch() throws InterruptedException, NoHrefException {
        // if we've already fetched
        if (isFetched()) {
            // return self
            return this;
        } else {
            // otherwise do the real fetch
            performNetworkFetch();
            return this;
        }
    }

    /**
     * Asyncronously retrieve the value at a given key path
     *
     * @param keyPath Key path to retrieve
     * @param <T>     Expected type to retrieve
     * @return A task wrapping the get(keyPath) call that is run on the Hyper's context's async executor
     * @see #get(String)
     */
    public <T> Task<T> getAsync(final String keyPath) {
        final Task<T>.TaskCompletionSource result = Task.create();
        Task.call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    result.setResult((T) get(keyPath));
                } catch (ClassCastException ex) {
                    result.setError(new WrongDataTypeException(ex));
                } catch (Exception ex) {
                    result.setError(ex);
                }
                return null;
            }
        }, getContext().getAsyncExecutor());
        return result.getTask();
    }

    /**
     * Perform the fetch of this object's data in the background.
     *
     * @return A task wrapping the fetch() call that is run on the Hyper's context's async executor
     * @see #fetch()
     */
    public Task<Hyper> fetchAsync() {
        final Task<Hyper>.TaskCompletionSource result = Task.create();
        Task.call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    result.setResult(fetch());
                } catch (Exception ex) {
                    result.setError(ex);
                }
                return null;
            }
        }, getContext().getAsyncExecutor());
        return result.getTask();
    }

    /**
     * Grab each item in this object's collection.
     * <br/>
     * This method has an optional argument to also fetch each item's data if it happens to be a
     * Hyper node with an href.
     *
     * @param prefetch Prefetch each Hyper node (with an href) in the collection
     * @param <T>      Type of items to expect back in the list
     * @return A task wrapping the each() call that is run on the Hyper's context's async executor
     * performing network requests on the Hyper's context's network executor.
     * @see #each()
     */
    public <T> Task<List<T>> eachAsync(final boolean prefetch) {
        final Task<List<T>>.TaskCompletionSource result = Task.create();
        Task.call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<T> each;
                try {
                    each = each();
                } catch (Exception ex) {
                    result.setError(ex);
                    return null;
                }
                if (prefetch) {
                    for (Object obj : each) {
                        if (obj instanceof Hyper && ((Hyper) obj).getHref() != null) {
                            ((Hyper) obj).fetch();
                        }
                    }
                }
                result.setResult(each);
                return null;
            }
        }, getContext().getAsyncExecutor());
        return result.getTask();
    }

    @Override
    public String toString() {
        return getFriendlyKeyPath();
    }

    /**
     * Build a request for retrieving an href.
     * <br/>
     * Take this opportunity to set appropriate request headers.
     *
     * @param href URL to retrieve
     * @return The request that will be used to pull data from the remote server.
     */
    protected abstract Request buildRequest(URL href);

    /**
     * Parse a response into Data
     *
     * @param response Response to parse
     * @return The wrapped Data
     * @throws io.hypergroup.hyper.exception.DataParseException When parsing goes wrong
     * @throws java.io.IOException                              When IO goes wrong
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
    protected abstract Hyper createHyperNodeFromData(String keyPath, URL relativeHref, Data data);

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

        String relativePath;
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
            relativePath = keyPath;
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
        /* default */ boolean isMultiKey() {
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
