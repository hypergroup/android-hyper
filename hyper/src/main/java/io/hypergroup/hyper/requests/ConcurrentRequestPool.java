package io.hypergroup.hyper.requests;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import bolts.Continuation;
import bolts.Task;

/**
 * When multiple requests are made to the same URL at the same time, they should share the same results.
 * <br/>
 * This class facilitates that by providing a Task that is returned for each request. The same promise is returned for the same request.
 */
public class ConcurrentRequestPool {

    /**
     * Tag used for logging
     */
    private static final String TAG = "ConcurrentRequestPool";

    /**
     * Map that holds the Tasks for each url for each method
     * <br/>
     * ex: mPool.get("GET").get("http://example.com/foo")
     */
    private Map<String, Map<String, Task<ResponsePackage>>> mPool = new ConcurrentHashMap<String, Map<String, Task<ResponsePackage>>>();

    /**
     * Construct this bad boy
     */
    public ConcurrentRequestPool() {
    }

    /**
     * Return a promise for a response body given your Request
     *
     * @param client   Client to make the connection
     * @param executor Executor responsible for the network requests
     * @param request  The actual request to make
     * @return A Task containing the response body. This may or may not be a new promise.
     */
    public Task<ResponsePackage> getResponseTask(final OkHttpClient client, final Executor executor, Request request) {
        Task<ResponsePackage> responseTask = getCurrentRequestTask(request);
        // if it doesn't exist
        if (responseTask == null) {
            // create it
            responseTask = createResponseTask(client, executor, request);
            // and save it
            saveResponseTask(request.method(), request.urlString(), responseTask);
        }
        return responseTask;
    }

    /**
     * Return a {@code Task<String>} for a Request already in action
     *
     * @param request The request to find a {@code Task<String>} for
     * @return An existing Task or null
     */
    protected Task<ResponsePackage> getCurrentRequestTask(Request request) {
        return getRequestMapByMethod(request.method()).get(request.urlString());
    }

    /**
     * Create/return a Map between String URLs and {@code Task<String>}s
     *
     * @param method The HTTP Method block of {@code Task<String>} Map to retrieve
     * @return Either a new or existing {@code Map<String, Task<String>>}
     */
    protected Map<String, Task<ResponsePackage>> getRequestMapByMethod(String method) {
        Map<String, Task<ResponsePackage>> requests = mPool.get(method);
        // if doesn't exist
        if (requests == null) {
            // create it
            requests = new HashMap<String, Task<ResponsePackage>>();
            // and save it
            mPool.put(method, requests);
        }
        return requests;
    }

    /**
     * Create an actual network calling task.
     *
     * @param client   Client to make the connection
     * @param executor Executor responsible for the network requests
     * @param request  The actual request to make
     * @return A new {@code Task<ResponsePackage>} being executed with the given Executor
     */
    protected Task<ResponsePackage> createResponseTask(final OkHttpClient client, final Executor executor, final Request request) {
        Task<ResponsePackage> responseTask = Task.call(new Callable<ResponsePackage>() {
            @Override
            public ResponsePackage call() throws Exception {
                // log outgoing
                Log.v(TAG, ">>> " + request.urlString());
                // get a response
                Response response = client.newCall(request).execute();
                // log incoming
                Log.v(TAG, "<<< " + request.urlString());
                // return that result
                return new ResponsePackage(response);
            }
        }, executor);
        // after that is done, we need to remove the {@code Task<String>} from the pool
        responseTask.continueWith(new Continuation<ResponsePackage, Void>() {
            @Override
            public Void then(Task<ResponsePackage> task) throws Exception {
                // call to remove the request
                removeResponseTask(request.method(), request.urlString());
                return null;
            }
        });
        return responseTask;
    }

    /**
     * Save a {@code Task<String>} to the pool
     *
     * @param method Method key
     * @param url    Url key
     * @param task   Task to store
     */
    protected void saveResponseTask(String method, String url, Task<ResponsePackage> task) {
        getRequestMapByMethod(method).put(url, task);
    }

    /**
     * Remove a {@code Task<String>} from the pool
     *
     * @param method Method key
     * @param url    Url key
     */
    protected Task<ResponsePackage> removeResponseTask(String method, String url) {
        return getRequestMapByMethod(method).remove(url);
    }
}
