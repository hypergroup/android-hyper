package io.hypergroup.hyper;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for a map of Key paths to Hyper nodes, used for local caching of Node objects
 */
public class HyperCache {

    private static final int DEFAULT_CAPACITY = 32;

    private Map<String, Hyper> mMap;


    /**
     * Construct this cache with the default configuration
     */
    public HyperCache() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Construct this cache with an initial capacity
     *
     * @param capacity Initial capacity of this cache
     */
    public HyperCache(int capacity) {
        mMap = new HashMap<String, Hyper>(capacity);
    }

    /**
     * Get a Hyper node at the given Key path or null if it doesn't exist
     *
     * @param keyPath Key path to retrieve a node from
     * @return Hyper instance or null
     */
    public Hyper get(String keyPath) {
        Hyper node = mMap.get(keyPath);
        //Log.v("HyperCache", ">>> " + keyPath + ": " + (node == null ? null : node.getHref()));
        return node;
    }

    /**
     * Save a Hyper node to this cache
     *
     * @param node Node to save
     */
    public void save(Hyper node) {
        String keyPath = node.getKeyPath();
        mMap.put(keyPath, node);
        //Log.v("HyperCache", "<<< " + keyPath + ": " + node.getHref());
    }

    /**
     * Invalidate a specific hyper node
     */
    public void invalidate(Hyper node) {
        String keyPath = node.getKeyPath();
        invalidate(keyPath);
    }

    /**
     * Invalidate whatever node exists at the given key path
     */
    public void invalidate(String keyPath) {
        mMap.remove(keyPath);
    }


}
