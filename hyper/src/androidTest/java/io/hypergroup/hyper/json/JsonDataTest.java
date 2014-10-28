package io.hypergroup.hyper.json;

import android.test.AndroidTestCase;

import org.json.JSONObject;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import io.hypergroup.hyper.exception.InvalidCollectionException;
import io.hypergroup.hyper.exception.MissingPropertyException;
import io.hypergroup.hyper.exception.NoHrefException;

public class JsonDataTest extends AndroidTestCase {


    public JsonDataTest() {
        super();
    }

    public void testHasProperty() throws Exception {
        JSONObject json = new JSONObject(JsonFiles.ROOT.GET);
        JsonData data = new JsonData(json);

        assertTrue("has current_user", data.hasProperty("current_user"));
        assertFalse("has asanta-sana-squash-banana", data.hasProperty("asanta-sana-squash-banana"));
    }

    public void testGetProperty() throws Exception {
        JSONObject json = new JSONObject(JsonFiles.USER.GET);
        JsonData data = new JsonData(json);

        assertEquals(data.getProperty("first_name"), "Matt");
    }

    public void testGetPropertyFailsAsExpected() throws Exception {
        JSONObject json = new JSONObject(JsonFiles.USER.GET);
        JsonData data = new JsonData(json);

        Exception error = null;
        try {
            data.getProperty("asanta-sana-squash-banana");
        } catch (Exception ex) {
            error = ex;
        }
        assertNotNull(error);
        assertSame(error.getClass(), MissingPropertyException.class);
    }

    public void testGetHref() throws Exception {
        JSONObject json = new JSONObject(JsonFiles.USER.GET);
        JsonData data = new JsonData(json);

        URL href = data.getHref(new URL("http://example.com"));

        assertEquals("/api/users/1", href.getPath());
    }

    public void testGetHrefNoHref() throws Exception {
        JSONObject json = new JSONObject();
        JsonData data = new JsonData(json);

        Exception error = null;
        try {
            data.getHref(new URL("http://example.com"));
        } catch (Exception ex) {
            error = ex;
        }

        assertNotNull(error);
        assertSame(error.getClass(), NoHrefException.class);
        assertEquals(error.getMessage(), "Valid href not found");
    }

    public void testGetHrefEmptyHref() throws Exception {
        JSONObject json = new JSONObject(JsonFiles.BASICS.EMPTY_HREF);
        JsonData data = new JsonData(json);

        URL href = data.getHref(new URL("http://example.com"));

        assertEquals("", href.getPath());
    }

    public void testGetHrefNullHref() throws Exception {
        JSONObject json = new JSONObject(JsonFiles.BASICS.NULL_HREF);
        JsonData data = new JsonData(json);

        Exception error = null;
        try {
            data.getHref(new URL("http://example.com"));
        } catch (Exception ex) {
            error = ex;
        }

        assertNotNull(error);
        assertSame(error.getClass(), NoHrefException.class);
        assertEquals(error.getMessage(), "Found null href");
    }


    public void testKeys() throws Exception {
        JSONObject json = new JSONObject(JsonFiles.USER.GET);
        JsonData data = new JsonData(json);

        String[] keys = data.getKeys();

        assertNotNull(keys);
        assertEquals(12, keys.length);

        HashSet<String> keySet = new HashSet<String>(Arrays.asList(keys));
        assertTrue("has key id", keySet.contains("id"));
        assertTrue("has key first_name", keySet.contains("first_name"));
        assertTrue("has key last_name", keySet.contains("last_name"));
        assertTrue("has key display_name", keySet.contains("display_name"));
        assertTrue("has key created_on", keySet.contains("created_on"));
        assertTrue("has key public", keySet.contains("public"));
        assertTrue("has key avatar", keySet.contains("avatar"));
        assertTrue("has key groups", keySet.contains("groups"));
        assertTrue("has key friends", keySet.contains("friends"));
        assertTrue("has key notes", keySet.contains("notes"));
        assertTrue("has key root", keySet.contains("root"));
        assertTrue("has key href", keySet.contains("href"));
    }

    public void testCollection() throws Exception {
        JSONObject json = new JSONObject(JsonFiles.FRIENDS.GET);
        JsonData data = new JsonData(json);

        List<Object> collection = data.getCollection();

        assertNotNull(collection);
        assertEquals(20, collection.size());

        for (Object obj : collection) {
            assertSame(obj.getClass(), JsonData.class);
        }
    }

    public void testCollectionNoCollection() throws Exception {
        JSONObject json = new JSONObject(JsonFiles.BASICS.EMPTY);
        JsonData data = new JsonData(json);

        Exception error = null;
        try {
            data.getCollection();
        } catch (Exception ex) {
            error = ex;
        }

        assertNotNull(error);
        assertSame(error.getClass(), InvalidCollectionException.class);
        assertEquals(error.getMessage(), "Collection not found");
    }

    public void testCollectionNullCollection() throws Exception {
        JSONObject json = new JSONObject(JsonFiles.BASICS.NULL_COLLECTION);
        JsonData data = new JsonData(json);

        Exception error = null;
        try {
            data.getCollection();
        } catch (Exception ex) {
            error = ex;
        }

        assertNotNull(error);
        assertSame(error.getClass(), InvalidCollectionException.class);
        assertEquals(error.getMessage(), "Collection not found");
    }
}
