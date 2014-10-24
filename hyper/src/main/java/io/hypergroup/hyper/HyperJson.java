package io.hypergroup.hyper;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.hypergroup.hyper.exception.DataParseException;
import io.hypergroup.hyper.exception.InvalidCollectionException;
import io.hypergroup.hyper.exception.MissingPropertyException;
import io.hypergroup.hyper.exception.NoHrefException;
import io.hypergroup.hyper.requests.ResponsePackage;

/**
 * hyper+json implementation of hypermedia.
 * <br/>
 * Hyper root = HyperJson.createRoot("http://example.com/api/v1/root");
 * <br/>
 * ...boom
 */
public class HyperJson extends Hyper {

    /**
     * HTTP Header for Accept
     */
    private static final String HEADER_ACCEPT = "Accept";

    /**
     * Accept application/hyper+json at top priority
     */
    private static final String ACCEPT_HYPER_JSON = "application/hyper+json; q=1";

    /**
     * Accept application/json at a high priority
     */
    private static final String ACCEPT_JSON = "application/json; q=0.95";

    /**
     * Accept text at a low priority
     */
    private static final String ACCEPT_TEXT = "text/plain; q=0.1";

    /**
     * Accept anything at a low priority
     */
    private static final String ACCEPT_ANY = "*/*; q=0";

    /**
     * Key used to extract collections
     */
    private static final String KEY_COLLECTION = "collection";

    /**
     * Key used to extract hrefs
     */
    private static final String KEY_HREF = "href";

    /**
     * Tag used for logging
     */
    private static final String TAG = HyperJson.class.getSimpleName();

    /**
     * Construct a default http client
     */
    private static OkHttpClient defaultClient() {
        // the default client is bare-bones
        return new OkHttpClient();
    }

    /**
     * Construct a cache for OkHttp
     *
     * @param context      Context to find a suitable cache directory for
     * @param cacheDirName Cache sub-directory name
     * @param cacheSize    Cache size in bytes
     * @return A new cache or null if the operation fails
     */
    private static Cache createCache(Context context, String cacheDirName, long cacheSize) {
        // Create an HTTP cache in the application cache directory.
        Cache cache = null;
        // the cache sub directory is located under the context's cache directory
        File cacheDir = new File(context.getCacheDir(), cacheDirName);
        try {
            // attempt to create a cache
            cache = new Cache(cacheDir, cacheSize);
        } catch (IOException e) {
            // log warning on failure, not having a cache is not necessarily an app-breaking thing
            // this behavior is potentially unwanted, but the benefits of not having random devices
            // breaking because of odd drivers (I'm looking at you Samsung) outweighs the cost, IMO
            Log.w(TAG, "Unable to create disk cache.", e);
        }
        return cache;
    }

    /**
     * Create a new root HyperJson node at the given URL
     *
     * @param url URL that is the root of the hypermedia
     * @return The newly created root Hyper node
     */
    public static Hyper createRoot(String url) {
        // bare bones root node
        return createRoot(url, defaultClient());
    }

    /**
     * Create a new root HyperJson node at the given url that also caches requests to disk
     *
     * @param url       URL that is the root of the hypermedia
     * @param context   Context to find a suitable cache directory for
     * @param cacheDir  Cache sub-directory name
     * @param cacheSize Cache size in bytes
     * @return The newly created root Hyper node
     */
    public static Hyper createCachedRoot(String url, Context context, String cacheDir, long cacheSize) {
        // use the default client
        OkHttpClient client = defaultClient();
        // apply the cache
        client.setCache(createCache(context, cacheDir, cacheSize));
        // return a node
        return createRoot(url, client);
    }

    /**
     * Create a new root HyperJson node at the given url with your http client
     *
     * @param url    URL that is the root of the hypermedia
     * @param client Your configured client
     * @return The newly created root Hyper node
     */
    public static Hyper createRoot(String url, OkHttpClient client) {
        // Build context
        HyperContext context = new HyperContext.Builder()
                .setHttpClient(client) // set the client
                .setHyperCache(new HyperCache()) // set the cache
                .build(); // build it
        // create the root node
        Hyper node = new HyperJson(null, url, context);
        // don't forget to set the root
        context.setRoot(node);
        return node;
    }

    /* default */ HyperJson(String keyPath, String href, HyperContext context) {
        super(keyPath, href, context);
    }

    /* default */ HyperJson(String keyPath, Data data, HyperContext context) {
        super(keyPath, data, context);
    }

    @Override
    protected Request buildRequest(String href) {
        return new Request.Builder()
                .url(mHref)
                .addHeader(HEADER_ACCEPT, ACCEPT_HYPER_JSON)// accept and prefer hyper+json
                .addHeader(HEADER_ACCEPT, ACCEPT_JSON)// accept json
                .addHeader(HEADER_ACCEPT, ACCEPT_TEXT)// accept text also
                .addHeader(HEADER_ACCEPT, ACCEPT_ANY)// accept anything really, if parsing fails, then we have problems
                .build(); // build that request
    }

    @Override
    protected Data parseResponse(ResponsePackage response) throws IOException, DataParseException {
        // get the body from the response
        String body = response.getBody();
        // try to parse the response
        JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException ex) {
            // fail sometimes
            throw new DataParseException("Error parsing JSON", ex);
        }
        // wrap JSONObject with the Data interface
        return new JsonData(json);
    }

    @Override
    protected Hyper createHyperNodeFromData(String keyPath, Data data) {
        return new HyperJson(keyPath, data, getContext());
    }

    @Override
    protected boolean isRawData(Object value) {
        return value instanceof JSONObject;
    }

    @Override
    protected Data createDataFromRawData(Object raw) {
        return new JsonData((JSONObject) raw);
    }

    /**
     * JSON implementation of Hyper.Data
     * <br/>
     * Thinly wraps JSONObject and its respective errors
     */
    public static class JsonData implements Data {

        /**
         * Wrapped object
         */
        private JSONObject mData;

        /**
         * Create a new JsonData as a wrapper for a JSONObject
         *
         * @param wrap Object to wrap
         */
        public JsonData(JSONObject wrap) {
            mData = wrap;
        }

        @Override
        public boolean hasProperty(String key) {
            return mData.has(key);
        }

        @Override
        public Object getProperty(String key) throws MissingPropertyException {
            try {
                // get the property
                return mData.get(key);
            } catch (JSONException ex) {
                // fail with a standardized exception
                throw new MissingPropertyException("Property not found", ex);
            }
        }

        @Override
        public List<Object> getCollection() throws InvalidCollectionException {
            JSONArray collection;
            try {
                // get the collection
                collection = mData.getJSONArray(KEY_COLLECTION);
            } catch (JSONException ex) {
                // fail with a standardized exception
                throw new InvalidCollectionException("Collection not found", ex);
            }
            if (collection != null) {
                try {
                    // for each item in the collection
                    int N = collection.length();
                    List<Object> items = new ArrayList<Object>(N);
                    for (int index = 0; index < N; index++) {
                        Object item = collection.get(index);
                        // if it is a JSONObject
                        if (item instanceof JSONObject) {
                            // wrap it as JsonData
                            item = new JsonData((JSONObject) item);
                        }
                        // save the item
                        items.add(item);
                    }
                    return items;
                } catch (JSONException indexError) {
                    throw new InvalidCollectionException("Index error while parsing collection", indexError);
                }
            } else {
                throw new InvalidCollectionException("null collection");
            }
        }

        @Override
        public String getHref() throws NoHrefException {
            try {
                // try to get a string named "href"
                return mData.getString(KEY_HREF);
            } catch (JSONException ex) {
                // fail with a standardized exception
                throw new NoHrefException("valid href not found", ex);
            }
        }
    }
}
