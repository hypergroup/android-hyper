package io.hypergroup.hyper.json;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.TestCase;

import java.util.List;

import bolts.Task;
import io.hypergroup.hyper.Data;
import io.hypergroup.hyper.Hyper;
import io.hypergroup.hyper.exception.IndexErrorException;
import io.hypergroup.hyper.exception.InvalidCollectionException;
import io.hypergroup.hyper.exception.MissingPropertyException;
import io.hypergroup.hyper.exception.NoHrefException;

public class HyperJsonTest extends TestCase {

    public HyperJsonTest() {
        super();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRequestHeaders() throws Exception {

        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
        server.play();

        // Create our hyper node using the mock server
        Hyper hyper = HyperJsons.createRoot(server.getUrl(JsonFiles.ROOT.URL));

        // Perform a fetch for testing
        Task<Hyper> task = hyper.fetchAsync();
        task.waitForCompletion();

        // test error
        assertFalse(task.isFaulted());
        assertNull(task.getError());
        assertNotNull(task.getResult());

        // test request headers are set
        assertEquals(1, server.getRequestCount());

        RecordedRequest request = server.takeRequest();
        assertNotNull(request.getHeader("Accept"));

        // shut down the server
        server.shutdown();
    }

    public void testRootFetch() throws Exception {

        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.ROOT.URL));

        // Perform a fetch for testing
        Task<Hyper> task = root.fetchAsync();
        task.waitForCompletion();

        // test error
        assertFalse(task.isFaulted());
        assertNull(task.getError());
        assertNotNull(task.getResult());

        // test request
        assertEquals(1, server.getRequestCount());
        RecordedRequest request = server.takeRequest();
        assertEquals(JsonFiles.ROOT.URL, request.getPath());

        // test fetch
        assertSame(root, task.getResult());

        // test data
        Data data = root.getData();
        assertNotNull(data);
        assertNotNull(data.getHref(root.getHref()));
        assertTrue(data.hasProperty("current_user"));
        assertTrue(data.hasProperty("users"));

        // shut down the server
        server.shutdown();
    }

    public void testHyperNodeKeyFetch() throws Exception {

        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.ROOT.URL));

        // Perform a fetch for testing
        Task<String> task = root.getAsync("current_user.first_name");
        task.waitForCompletion();

        // test error
        assertFalse(task.isFaulted());
        assertNull(task.getError());
        assertNotNull(task.getResult());

        // test requests
        assertEquals(2, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.ROOT.URL, request1.getPath());
        RecordedRequest request2 = server.takeRequest();
        assertEquals(JsonFiles.USER.URL.replace("<user_id>", "1"), request2.getPath());

        // test data
        String firstName = task.getResult();
        assertEquals("Matt", firstName);

        // shut down the server
        server.shutdown();
    }

    public void testHydratedNodeNodeAlreadyFetched() throws Exception {

        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.ROOT.URL));

        // Perform a fetch for testing
        Task<Integer> task = root.getAsync("current_user.friends.count");
        task.waitForCompletion();

        // test error
        assertFalse(task.isFaulted());
        assertNull(task.getError());
        assertNotNull(task.getResult());

        // test requests
        assertEquals(2, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.ROOT.URL, request1.getPath());
        RecordedRequest request2 = server.takeRequest();
        assertEquals(JsonFiles.USER.URL.replace("<user_id>", "1"), request2.getPath());

        // test data
        Object count = task.getResult();
        assertEquals(10, count);

        // shut down the server
        server.shutdown();
    }

    public void testCollection() throws Exception {

        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.FRIENDS.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.FRIENDS.URL.replace("<user_id>", "1")));
        Task<Hyper> fetchTask = root.fetchAsync();
        fetchTask.waitForCompletion();

        List<Hyper> friends = root.each();

        // test error
        assertFalse(fetchTask.isFaulted());
        assertNull(fetchTask.getError());
        assertNotNull(fetchTask.getResult());

        // test requests
        assertEquals(1, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.FRIENDS.URL.replace("<user_id>", "1"), request1.getPath());

        // test data
        assertNotNull(friends);
        assertEquals("Expected length", 20, friends.size());

        // shut down the server
        server.shutdown();

    }

    public void testNoCollection() throws Exception {
        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.ROOT.URL));
        Task<Hyper> fetchTask = root.fetchAsync();
        fetchTask.waitForCompletion();

        Exception error = null;
        try {
            root.each();
        } catch (Exception ex) {
            error = ex;
        }

        // test error
        assertFalse(fetchTask.isFaulted());
        assertNull(fetchTask.getError());
        assertNotNull(fetchTask.getResult());

        // test HAS error
        assertNotNull(error);

        // test requests
        assertEquals(1, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.ROOT.URL, request1.getPath());

        // test data
        assertSame("Exception is InvalidCollectionException", error.getClass(), InvalidCollectionException.class);

        // shut down the server
        server.shutdown();
    }

    public void testNoProperty() throws Exception {
        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.ROOT.URL));
        Task<Hyper> fetchTask = root.fetchAsync();
        fetchTask.waitForCompletion();

        Task<Object> propertyTask = root.getAsync("banana-nana-fo-fanna");
        propertyTask.waitForCompletion();

        // test error
        assertFalse(fetchTask.isFaulted());
        assertNull(fetchTask.getError());
        assertNotNull(fetchTask.getResult());

        // test HAS error
        assertTrue(propertyTask.isFaulted());
        assertNotNull(propertyTask.getError());
        assertNull(propertyTask.getResult());

        // test requests
        assertEquals(1, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.ROOT.URL, request1.getPath());

        // grab results
        Exception error = propertyTask.getError();

        // test data
        assertSame("Exception is MissingPropertyException", error.getClass(), MissingPropertyException.class);

        // shut down the server
        server.shutdown();
    }

    public void testNoHref() throws Exception {
        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.USER.URL.replace("<user_id>", "1")));
        Task<Hyper> fetchTask = root.fetchAsync();
        fetchTask.waitForCompletion();

        assertFalse(fetchTask.isFaulted());
        assertNull(fetchTask.getError());
        assertNotNull(fetchTask.getResult());

        Task<Hyper> propertyTask = root.getAsync("avatar.default");
        propertyTask.waitForCompletion();

        assertFalse(propertyTask.isFaulted());
        assertNull(propertyTask.getError());
        assertNotNull(propertyTask.getResult());

        Task<Hyper> fetch2 = propertyTask.getResult().fetchAsync();
        fetch2.waitForCompletion();

        assertTrue(fetch2.isFaulted());
        assertNotNull(fetch2.getError());
        assertNull(fetch2.getResult());

        // test requests
        assertEquals(1, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.USER.URL.replace("<user_id>", "1"), request1.getPath());

        // grab results
        Exception error = fetch2.getError();

        // test data
        assertSame("Exception is NoHrefException", error.getClass(), NoHrefException.class);

        // shut down the server
        server.shutdown();
    }

    public void testCollectionItem() throws Exception {

        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.FRIENDS.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.FRIENDS.URL.replace("<user_id>", "1")));
        Task<Hyper> fetchTask = root.fetchAsync();
        fetchTask.waitForCompletion();

        assertFalse(fetchTask.isFaulted());
        assertNull(fetchTask.getError());
        assertNotNull(fetchTask.getResult());

        Hyper friend = root.get("0");

        // test requests
        assertEquals(1, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.FRIENDS.URL.replace("<user_id>", "1"), request1.getPath());

        // test data
        assertEquals("/api/users/1", friend.getHref().getPath());
        assertEquals("Matt", friend.get("first_name"));

        // shut down the server
        server.shutdown();

    }

    public void testDeepCollectionItem() throws Exception {

        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.FRIENDS.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.FRIENDS.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.USER.URL.replace("<user_id>", "1"))).fetch();

        Hyper friend = root.get("friends.0");

        // test requests
        assertEquals(2, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.USER.URL.replace("<user_id>", "1"), request1.getPath());
        RecordedRequest request2 = server.takeRequest();
        assertEquals(JsonFiles.FRIENDS.URL.replace("<user_id>", "1"), request2.getPath());

        // test data
        assertEquals("/api/users/1", friend.getHref().getPath());

        // shut down the server
        server.shutdown();

    }

    public void testCollectionItemIndexOOB() throws Exception {

        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.FRIENDS.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.FRIENDS.URL.replace("<user_id>", "1")));
        Task<Hyper> fetchTask = root.fetchAsync();
        fetchTask.waitForCompletion();

        assertFalse(fetchTask.isFaulted());
        assertNull(fetchTask.getError());
        assertNotNull(fetchTask.getResult());

        Exception error = null;
        try {
            root.get("1000");
        } catch (Exception ex) {
            error = ex;
        }

        // test requests
        assertEquals(1, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.FRIENDS.URL.replace("<user_id>", "1"), request1.getPath());

        // test data
        assertNotNull(error);
        assertSame(error.getClass(), IndexErrorException.class);

        // shut down the server
        server.shutdown();

    }

    public void testCollectionItemInvalidatedNotCached() throws Exception {
        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.FRIENDS.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.FRIENDS.URL.replace("<user_id>", "1"))).fetch();
        Hyper friend1 = root.get("0");
        friend1.invalidate();
        Hyper friend2 = root.get("0");

        // test requests
        assertEquals(1, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.FRIENDS.URL.replace("<user_id>", "1"), request1.getPath());

        // test data
        assertEquals("/api/users/1", friend1.getHref().getPath());
        assertEquals("/api/users/1", friend2.getHref().getPath());
        assertNotSame("test that friend1 != friend2", friend1, friend2);

        // shut down the server
        server.shutdown();
    }

    public void testKeyPathCollectionDeep() throws Exception {
        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USERS.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USERS.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.ROOT.URL)).fetch();
        Hyper user = root.get("users.0");

        // test requests
        assertEquals(2, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.ROOT.URL, request1.getPath());
        RecordedRequest request2 = server.takeRequest();
        assertEquals(JsonFiles.USERS.URL, request2.getPath());

        // test data
        assertEquals("users.0", user.getKeyPath());

        // shut down the server
        server.shutdown();
    }

    public void testKeyPathImmediateCollection() throws Exception {
        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.FRIENDS.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USERS.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USERS.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.FRIENDS.URL.replace("<user_id>", "1"))).fetch();
        Hyper user = root.get("0");

        // test requests
        assertEquals(1, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.FRIENDS.URL.replace("<user_id>", "1"), request1.getPath());

        // test data
        assertEquals("0", user.getKeyPath());

        // shut down the server
        server.shutdown();
    }

    public void testKeyPathDeep() throws Exception {
        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USERS.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USERS.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.ROOT.URL)).fetch();
        Hyper user = root.get("users.search.input.search");

        // test requests
        assertEquals(1, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.ROOT.URL, request1.getPath());

        // test data
        assertEquals("users.search.input.search", user.getKeyPath());

        // shut down the server
        server.shutdown();
    }

    public void testKeyPathImmediate() throws Exception {
        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USERS.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USERS.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.ROOT.URL)).fetch();
        Hyper user = root.get("users");

        // test requests
        assertEquals(1, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.ROOT.URL, request1.getPath());

        // test data
        assertEquals("users", user.getKeyPath());

        // shut down the server
        server.shutdown();
    }

    public void testMergeAfterFetch() throws Exception {
        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.FRIENDS.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.FRIENDS.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJsons.createRoot(server.getUrl(JsonFiles.USER.URL.replace("<user_id>", "1"))).fetch();
        Hyper friends = root.get("friends");
        Integer count1 = friends.get("count");
        friends.fetch();
        Integer count2 = friends.get("count");

        // test requests
        assertEquals(2, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.USER.URL.replace("<user_id>", "1"), request1.getPath());
        RecordedRequest request2 = server.takeRequest();
        assertEquals(JsonFiles.FRIENDS.URL.replace("<user_id>", "1"), request2.getPath());

        // test data
        assertEquals((Object) 10, count1);
        assertEquals((Object) 10, count2);

        // shut down the server
        server.shutdown();
    }


}
