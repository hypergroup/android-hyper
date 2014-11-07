package io.hypergroup.hyper;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.Executor;

import bolts.Task;

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
         * Override the default async Executor pool
         */
        public Builder setAsyncExecutor(Executor AsyncExecutor) {
            mAsyncExecutor = AsyncExecutor;
            return this;
        }

        public HyperContext build() {
            HyperContext context = new HyperContext();
            context.setRoot(mRoot);
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
