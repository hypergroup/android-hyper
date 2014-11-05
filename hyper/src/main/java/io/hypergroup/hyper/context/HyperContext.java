package io.hypergroup.hyper.context;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.Executor;

import bolts.Task;
import io.hypergroup.hyper.Hyper;

/**
 * Encapsulates functionality that is transferred from a Hyper node to the next new Hyper node
 */
public class HyperContext {

    /**
     * Default executor for network requests
     */
    public static final Executor NETWORK_EXECUTOR = Task.BACKGROUND_EXECUTOR;
    public static final Executor ASYNC_EXECUTOR = Task.BACKGROUND_EXECUTOR;

    /**
     * Root node
     */
    private Hyper mRoot;

    /**
     * Http client
     */
    private OkHttpClient mHttpClient;


    /**
     * Executor for network requests
     */
    private Executor mNetworkExecutor;

    /**
     * Executor for async fetch task
     */
    private Executor mAsyncExecutor;

    /* default */ HyperContext() {
    }

    public Hyper getRoot() {
        return mRoot;
    }

    public void setRoot(Hyper root) {
        mRoot = root;
    }

    public OkHttpClient getHttpClient() {
        return mHttpClient;
    }

    public void setHttpClient(OkHttpClient httpClient) {
        mHttpClient = httpClient;
    }

    public Executor getNetworkExecutor() {
        return mNetworkExecutor;
    }

    public void setNetworkExecutor(Executor networkExecutor) {
        mNetworkExecutor = networkExecutor;
    }

    public Executor getAsyncExecutor() {
        return mAsyncExecutor;
    }

    public void setAsyncExecutor(Executor AsyncExecutor) {
        mAsyncExecutor = AsyncExecutor;
    }

    /**
     * Build a HyperContext
     */
    public static class Builder {

        private Hyper mRoot;
        private OkHttpClient mHttpClient = null;
        private Executor mNetworkExecutor = NETWORK_EXECUTOR;
        private Executor mAsyncExecutor = ASYNC_EXECUTOR;

        //private HyperCache mHyperCache;
        public void Builder() {

        }

        /**
         * This should only be called by functions that create root nodes
         */
        public Builder setRoot(Hyper root) {
            mRoot = root;
            return this;
        }

        public Builder setHttpClient(OkHttpClient httpClient) {
            mHttpClient = httpClient;
            return this;
        }

        /**
         * Override the default Network Executor
         */
        public Builder setNetworkExecutor(Executor networkExecutor) {
            mNetworkExecutor = networkExecutor;
            return this;
        }

        /**
         * Override the default Fetch Executor
         */
        public Builder setAsyncExecutor(Executor AsyncExecutor) {
            mAsyncExecutor = AsyncExecutor;
            return this;
        }

        public HyperContext build() {
            HyperContext context = new HyperContext();
            context.setRoot(mRoot);
            context.setNetworkExecutor(mNetworkExecutor);
            context.setAsyncExecutor(mAsyncExecutor);

            OkHttpClient client = mHttpClient;
            if (client == null) {
                client = new OkHttpClient();
            }
            context.setHttpClient(client);

            return context;
        }
    }
}
