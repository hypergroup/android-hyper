package io.hypergroup.hyper.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.hypergroup.hyper.Data;
import io.hypergroup.hyper.exception.InvalidCollectionException;
import io.hypergroup.hyper.exception.MissingPropertyException;
import io.hypergroup.hyper.exception.NoHrefException;

/**
 * JSON implementation of Hyper.Data
 * <br/>
 * Thinly wraps JSONObject and its respective errors
 */
public class JsonData implements Data {

    /**
     * Key used to extract collections
     */
    private static final String KEY_COLLECTION = "collection";

    /**
     * Key used to extract hrefs
     */
    private static final String KEY_HREF = "href";

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
