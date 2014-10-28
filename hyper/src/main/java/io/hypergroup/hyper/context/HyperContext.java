package io.hypergroup.hyper.context;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.Executor;

import bolts.Task;
import io.hypergroup.hyper.Hyper;
import io.hypergroup.hyper.context.cache.HyperCache;
import io.hypergroup.hyper.context.requests.ConcurrentRequestPool;

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
     * Local caching mechanism
     */
    private HyperCache mHyperCache;

    /**
     * Concurrent request saving mechanism
     */
    private ConcurrentRequestPool mConcurrentRequestPool;

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

    public HyperCache getHyperCache() {
        return mHyperCache;
    }

    public void setHyperCache(HyperCache hyperCache) {
        mHyperCache = hyperCache;
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

    public ConcurrentRequestPool getConcurrentRequestPool() {
        return mConcurrentRequestPool;
    }

    /* default */ void setConcurrentRequestPool(ConcurrentRequestPool concurrentRequestPool) {
        mConcurrentRequestPool = concurrentRequestPool;
    }

    /**
     * Build a HyperContext
     */
    public static class Builder {

        private Hyper mRoot;
        private OkHttpClient mHttpClient;
        private HyperCache mHyperCache;
        private Executor mNetworkExecutor = NETWORK_EXECUTOR;
        private Executor mAsyncExecutor = ASYNC_EXECUTOR;
        private ConcurrentRequestPool mConcurrentRequestPool = new ConcurrentRequestPool();

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

        public Builder setHyperCache(HyperCache hyperCache) {
            mHyperCache = hyperCache;
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
         * Override the default concurrent request pool
         */
        public Builder setConcurrentRequestPool(ConcurrentRequestPool pool) {
            mConcurrentRequestPool = pool;
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
            context.setHttpClient(mHttpClient);
            context.setRoot(mRoot);
            context.setHyperCache(mHyperCache);
            context.setNetworkExecutor(mNetworkExecutor);
            context.setConcurrentRequestPool(mConcurrentRequestPool);
            context.setAsyncExecutor(mAsyncExecutor);
            return context;
        }
    }
}
