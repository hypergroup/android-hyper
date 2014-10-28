package io.hypergroup.hyper.json;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
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
                // un-hittable, theoretically
                throw new InvalidCollectionException("Index error while parsing collection", indexError);
            }
        } else {
            return new ArrayList<Object>();
        }
    }

    @Override
    public URL getHref(URL relativeHref) throws NoHrefException {
        try {
            if (mData.has(KEY_HREF) && mData.isNull(KEY_HREF)) {
                throw new NoHrefException("Found null href");
            }
            // try to get a string named "href"
            String href = mData.getString(KEY_HREF);
            // if the href is empty
            if (TextUtils.isEmpty(href)) {
                // empty href
                return relativeHref;
            }
            // build a relative url
            return new URL(relativeHref, href);
        } catch (JSONException ex) {
            // fail with a standardized exception
            throw new NoHrefException("Valid href not found", ex);
        } catch (MalformedURLException ex) {
            // fail with a standardized exception
            throw new NoHrefException("Invalid href found", ex);
        }
    }

    @Override
    public String[] getKeys() {
        Iterator<String> iKeys = mData.keys();
        List<String> keys = new ArrayList<String>();
        while (iKeys.hasNext()) {
            keys.add(iKeys.next());
        }
        return keys.toArray(new String[keys.size()]);
    }
}
