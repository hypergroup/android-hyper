package io.hypergroup.hyper;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

import io.hypergroup.hyper.exception.InvalidCollectionException;
import io.hypergroup.hyper.exception.MissingPropertyException;
import io.hypergroup.hyper.exception.NoHrefException;

/**
 * Represents underlying data, a thin wrapper for object such as JSONObject.
 */
public interface Data extends Serializable {

    /**
     * Test for existence of a property in this data
     *
     * @param key Property name to test
     * @return Test results
     */
    public boolean hasProperty(String key);

    /**
     * Retrieve a property with the given key
     *
     * @param key Key to retrieve
     * @return Boxed primitives or wrapper objects
     * @throws io.hypergroup.hyper.exception.MissingPropertyException When the property does not exist
     */
    public Object getProperty(String key) throws MissingPropertyException;

    /**
     * Retrieve a collection from this source
     *
     * @return A list of boxed primitives or wrapper objects
     * @throws io.hypergroup.hyper.exception.InvalidCollectionException When the collection does not exist
     */
    public List<Object> getCollection() throws InvalidCollectionException;

    /**
     * Retrieve the href for wrapped data
     *
     * @param base Base URL, context for the new URL
     * @return The href for wrapped data
     * @throws io.hypergroup.hyper.exception.NoHrefException When the href property is missing entirely or null.
     */
    public URL getHref(URL base) throws NoHrefException;

    /**
     * Retrieve the list of keys for this data
     */
    public String[] getKeys();

    /**
     * Add additional data into the data structure
     *
     * @param data Data whose keys should be merged into this object, those keys should take precendence over the original data.
     */
    public void merge(Data data);

}
