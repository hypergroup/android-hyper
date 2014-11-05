package io.hypergroup.hyper.json;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import io.hypergroup.hyper.Hyper;
import io.hypergroup.hyper.HyperContext;

/**
 * Helper method for HyperJson
 */
public class HyperJsons {

    /**
     * Tag used for logging
     */
    private static final String TAG = HyperJsons.class.getSimpleName();

    /**
     * Default max cache size
     */
    private static final long CACHE_SIZE_BYTES = 1024 * 1024 * 16;

    /**
     * Default cache dir
     */
    private static final String CACHE_DIR = "hyper-cache";

    /**
     * Client creation functions
     */
    public static final class Client {

        /**
         * Create a cache for an OkHttpClient
         *
         * @param context   Android context to create the cache with
         * @param dirName   Directory name of the cache
         * @param sizeBytes Max size of the cache in bytes
         * @return A new cache or null if there is an IOException
         */
        public static Cache createCache(Context context, String dirName, long sizeBytes) {
            File dir = new File(context.getCacheDir(), dirName);
            try {
                return new Cache(dir, sizeBytes);
            } catch (IOException ex) {
                Log.w(TAG, "Unable to create cache: " + dir, ex);
                return null;
            }
        }

        /**
         * Create a cache for an OkHttpClient using default settings
         *
         * @param context Android context to create the cache with
         * @return A new cache or null if there is an IOException
         */
        public static Cache createCache(Context context) {
            return createCache(context, CACHE_DIR, CACHE_SIZE_BYTES);
        }

        /**
         * Create an OkHttpClient with a cache using default settings
         *
         * @param context Android context to create the cache with
         * @return A shiny new OkHttpClient
         */
        public static OkHttpClient createClient(Context context) {
            OkHttpClient client = new OkHttpClient();
            client.setCache(createCache(context));
            return client;
        }

        /**
         * Create an OkHttpClient with a cache
         *
         * @param context   Android context to create the cache with
         * @param dirName   Directory name of the cache
         * @param sizeBytes Max size of the cache in bytes
         * @return A shiny new OkHttpClient
         */
        public static OkHttpClient createClient(Context context, String dirName, long sizeBytes) {
            OkHttpClient client = new OkHttpClient();
            client.setCache(createCache(context, dirName, sizeBytes));
            return client;
        }
    }

    /**
     * Create new root HyperJson node with a cache directory
     *
     * @param context Android context to create the cache with
     * @param url     URL that is the root of the hypermedia
     * @return The newly created root Hyper node
     */
    public static Hyper createRoot(Context context, URL url) {
        OkHttpClient client = Client.createClient(context);
        HyperContext.Builder builder = new HyperContext.Builder()
            .setHttpClient(client);
        return createRoot(url, builder.build());
    }

    /**
     * Create a new root HyperJson node at the given URL
     *
     * @param url URL that is the root of the hypermedia
     * @return The newly created root Hyper node
     */
    public static Hyper createRoot(URL url) {
        // bare bones root node
        return createRoot(url, new HyperContext.Builder().build());
    }

    /**
     * Create a new root HyperJson node at the given url with your http client
     *
     * @param url          URL that is the root of the hypermedia
     * @param hyperContext Your configured HyperContext
     * @return The newly created root Hyper node
     */
    public static Hyper createRoot(URL url, HyperContext hyperContext) {
        // Build context
        // create the root node
        HyperJson node = new HyperJson(null, url, hyperContext);
        // don't forget to set the root
        hyperContext.setRoot(node);
        return node;
    }
}
