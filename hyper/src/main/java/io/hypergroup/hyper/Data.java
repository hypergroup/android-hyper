package io.hypergroup.hyper;

import java.util.List;

import io.hypergroup.hyper.exception.InvalidCollectionException;
import io.hypergroup.hyper.exception.MissingPropertyException;
import io.hypergroup.hyper.exception.NoHrefException;

/**
 * Represents underlying data, a thin wrapper for object such as JSONObject.
 */
public interface Data {

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
     * @return The href for wrapped data, null if it is null
     * @throws io.hypergroup.hyper.exception.NoHrefException When the href property is missing entirely, raise an exception
     */
    public String getHref() throws NoHrefException;

}
