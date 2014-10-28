package io.hypergroup.hyper;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.TestCase;

import bolts.Task;
import io.hypergroup.hyper.json.HyperJson;

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
//
//    public void testRequestHeaders() throws Exception {
//
//        // Create a mock server
//        MockWebServer server = new MockWebServer();
//        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
//        server.play();
//
//        // Create our hyper node using the mock server
//        Hyper hyper = HyperJson.createRoot(server.getUrl(JsonFiles.ROOT.URL));
//
//        // Perform a fetch for testing
//        Task<Hyper> task = hyper.fetch();
//        task.waitForCompletion();
//
//        // test error
//        assertFalse(task.isFaulted());
//        assertNull(task.getError());
//        assertNotNull(task.getResult());
//
//        // test request headers are set
//        assertEquals(1, server.getRequestCount());
//
//        RecordedRequest request = server.takeRequest();
//        assertNotNull(request.getHeader("Accept"));
//
//        // shut down the server
//        server.shutdown();
//    }
//
//    public void testRootFetch() throws Exception {
//
//        // Create a mock server
//        MockWebServer server = new MockWebServer();
//        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
//        server.play();
//
//        // Create our root node using the mock server
//        Hyper root = HyperJson.createRoot(server.getUrl(JsonFiles.ROOT.URL));
//
//        // Perform a fetch for testing
//        Task<Hyper> task = root.fetch();
//        task.waitForCompletion();
//
//        // test error
//        assertFalse(task.isFaulted());
//        assertNull(task.getError());
//        assertNotNull(task.getResult());
//
//        // test request
//        assertEquals(1, server.getRequestCount());
//        RecordedRequest request = server.takeRequest();
//        assertEquals(JsonFiles.ROOT.URL, request.getPath());
//
//        // test fetch
//        assertSame(root, task.getResult());
//
//        // test data
//        Data data = root.getData();
//        assertNotNull(data);
//        assertNotNull(data.getHref(root.getHref()));
//        assertTrue(data.hasProperty("current_user"));
//        assertTrue(data.hasProperty("users"));
//
//        // shut down the server
//        server.shutdown();
//    }
//
//    public void testHyperNodeKeyFetch() throws Exception {
//
//        // Create a mock server
//        MockWebServer server = new MockWebServer();
//        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
//        server.play();
//
//        // Create our root node using the mock server
//        Hyper root = HyperJson.createRoot(server.getUrl(JsonFiles.ROOT.URL));
//
//        // Perform a fetch for testing
//        Task<String> task = root.get("current_user.first_name");
//        task.waitForCompletion();
//
//        // test error
//        assertFalse(task.isFaulted());
//        assertNull(task.getError());
//        assertNotNull(task.getResult());
//
//        // test requests
//        assertEquals(2, server.getRequestCount());
//        RecordedRequest request1 = server.takeRequest();
//        assertEquals(JsonFiles.ROOT.URL, request1.getPath());
//        RecordedRequest request2 = server.takeRequest();
//        assertEquals(JsonFiles.USER.URL.replace("<user_id>", "1"), request2.getPath());
//
//        // test data
//        String firstName = task.getResult();
//        assertEquals("Matt", firstName);
//
//        // shut down the server
//        server.shutdown();
//    }
//
//    public void testHydratedNodeNodeAlreadyFetched() throws Exception {
//
//        // Create a mock server
//        MockWebServer server = new MockWebServer();
//        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
//        server.play();
//
//        // Create our root node using the mock server
//        Hyper root = HyperJson.createRoot(server.getUrl(JsonFiles.ROOT.URL));
//
//        // Perform a fetch for testing
//        Task<Integer> task = root.get("current_user.friends.count");
//        task.waitForCompletion();
//
//        // test error
//        assertFalse(task.isFaulted());
//        assertNull(task.getError());
//        assertNotNull(task.getResult());
//
//        // test requests
//        assertEquals(2, server.getRequestCount());
//        RecordedRequest request1 = server.takeRequest();
//        assertEquals(JsonFiles.ROOT.URL, request1.getPath());
//        RecordedRequest request2 = server.takeRequest();
//        assertEquals(JsonFiles.USER.URL.replace("<user_id>", "1"), request2.getPath());
//
//        // test data
//        Object count = task.getResult();
//        assertEquals(10, count);
//
//        // shut down the server
//        server.shutdown();
//    }
//
//    public void testDeepNodeIsCached() throws Exception {
//
//        // Create a mock server
//        MockWebServer server = new MockWebServer();
//        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
//        server.play();
//
//        // Create our root node using the mock server
//        Hyper root = HyperJson.createRoot(server.getUrl(JsonFiles.ROOT.URL));
//
//        // Perform a fetch for testing
//        Task<Hyper> fetchUserTask1 = root.<Hyper>get("current_user").continueWithTask(new Continuation<Hyper, Task<Hyper>>() {
//            @Override
//            public Task<Hyper> then(Task<Hyper> task) throws Exception {
//                return task.getResult().fetch();
//            }
//        });
//        fetchUserTask1.waitForCompletion();
//
//        // fetch the same object for testing
//        Task<Hyper> fetchUserTask2 = root.<Hyper>get("current_user").continueWithTask(new Continuation<Hyper, Task<Hyper>>() {
//            @Override
//            public Task<Hyper> then(Task<Hyper> task) throws Exception {
//                return task.getResult().fetch();
//            }
//        });
//        fetchUserTask2.waitForCompletion();
//
//        // perform tasks in parallel (2nd task initiated during first 1st)
//        fetchUserTask1.waitForCompletion();
//        fetchUserTask2.waitForCompletion();
//
//        // test error
//        assertFalse(fetchUserTask1.isFaulted());
//        assertNull(fetchUserTask1.getError());
//        assertNotNull(fetchUserTask1.getResult());
//        assertFalse(fetchUserTask2.isFaulted());
//        assertNull(fetchUserTask2.getError());
//        assertNotNull(fetchUserTask2.getResult());
//
//        // grab results
//        Hyper user1 = fetchUserTask1.getResult();
//        Hyper user2 = fetchUserTask2.getResult();
//
//        // test requests
//        assertEquals(2, server.getRequestCount());
//        RecordedRequest request1 = server.takeRequest();
//        assertEquals(JsonFiles.ROOT.URL, request1.getPath());
//        RecordedRequest request2 = server.takeRequest();
//        assertEquals(JsonFiles.USER.URL.replace("<user_id>", "1"), request2.getPath());
//
//        // test data
//        assertSame("user1 == user2", user1, user2);
//
//        // shut down the server
//        server.shutdown();
//    }
//
//    public void testDeepNodeIsCachedParallel() throws Exception {
//
//        // Create a mock server
//        MockWebServer server = new MockWebServer();
//        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
//        server.play();
//
//        // Create our root node using the mock server
//        Hyper root = HyperJson.createRoot(server.getUrl(JsonFiles.ROOT.URL));
//
//        // Perform a fetch for testing
//        Task<Hyper> fetchUserTask1 = root.<Hyper>get("current_user").continueWithTask(new Continuation<Hyper, Task<Hyper>>() {
//            @Override
//            public Task<Hyper> then(Task<Hyper> task) throws Exception {
//                return task.getResult().fetch();
//            }
//        });
//
//        // fetch the same object for testing
//        Task<Hyper> fetchUserTask2 = root.<Hyper>get("current_user").continueWithTask(new Continuation<Hyper, Task<Hyper>>() {
//            @Override
//            public Task<Hyper> then(Task<Hyper> task) throws Exception {
//                return task.getResult().fetch();
//            }
//        });
//
//        // perform tasks in parallel (2nd task initiated during first 1st)
//        fetchUserTask1.waitForCompletion();
//        fetchUserTask2.waitForCompletion();
//
//        // test error
//        assertFalse(fetchUserTask1.isFaulted());
//        assertNull(fetchUserTask1.getError());
//        assertNotNull(fetchUserTask1.getResult());
//        assertFalse(fetchUserTask2.isFaulted());
//        assertNull(fetchUserTask2.getError());
//        assertNotNull(fetchUserTask2.getResult());
//
//        // grab results
//        Hyper user1 = fetchUserTask1.getResult();
//        Hyper user2 = fetchUserTask2.getResult();
//
//        // test requests
//        assertEquals(2, server.getRequestCount());
//        RecordedRequest request1 = server.takeRequest();
//        assertEquals(JsonFiles.ROOT.URL, request1.getPath());
//        RecordedRequest request2 = server.takeRequest();
//        assertEquals(JsonFiles.USER.URL.replace("<user_id>", "1"), request2.getPath());
//
//        // test data
//        assertSame("user1 == user2", user1, user2);
//
//        // shut down the server
//        server.shutdown();
//    }
//
//    public void testCollection() throws Exception {
//
//        // Create a mock server
//        MockWebServer server = new MockWebServer();
//        server.enqueue(new MockResponse().setBody(JsonFiles.FRIENDS.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
//        server.play();
//
//        // Create our root node using the mock server
//        Hyper root = HyperJson.createRoot(server.getUrl(JsonFiles.FRIENDS.URL.replace("<user_id>", "1")));
//        Task<Hyper> fetchTask = root.fetch();
//        fetchTask.waitForCompletion();
//
//        Task<List<Hyper>> friendsTask = root.each();
//        friendsTask.waitForCompletion();
//
//        // test error
//        assertFalse(fetchTask.isFaulted());
//        assertNull(fetchTask.getError());
//        assertNotNull(fetchTask.getResult());
//        assertFalse(friendsTask.isFaulted());
//        assertNull(friendsTask.getError());
//        assertNotNull(friendsTask.getResult());
//
//        // test requests
//        assertEquals(1, server.getRequestCount());
//        RecordedRequest request1 = server.takeRequest();
//        assertEquals(JsonFiles.FRIENDS.URL.replace("<user_id>", "1"), request1.getPath());
//
//        // grab results
//        List<Hyper> friends = friendsTask.getResult();
//
//        // test data
//        assertNotNull(friends);
//        assertEquals("Expected length", 20, friends.size());
//
//        // shut down the server
//        server.shutdown();
//
//    }
//
//    public void testNoCollection() throws Exception {
//        // Create a mock server
//        MockWebServer server = new MockWebServer();
//        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
//        server.play();
//
//        // Create our root node using the mock server
//        Hyper root = HyperJson.createRoot(server.getUrl(JsonFiles.ROOT.URL));
//        Task<Hyper> fetchTask = root.fetch();
//        fetchTask.waitForCompletion();
//
//        Task<List<Hyper>> friendsTask = root.each();
//        friendsTask.waitForCompletion();
//
//        // test error
//        assertFalse(fetchTask.isFaulted());
//        assertNull(fetchTask.getError());
//        assertNotNull(fetchTask.getResult());
//
//        // test HAS error
//        assertTrue(friendsTask.isFaulted());
//        assertNotNull(friendsTask.getError());
//        assertNull(friendsTask.getResult());
//
//        // test requests
//        assertEquals(1, server.getRequestCount());
//        RecordedRequest request1 = server.takeRequest();
//        assertEquals(JsonFiles.ROOT.URL, request1.getPath());
//
//        // grab results
//        Exception error = friendsTask.getError();
//
//        // test data
//        assertSame("Exception is InvalidCollectionException", error.getClass(), InvalidCollectionException.class);
//
//        // shut down the server
//        server.shutdown();
//    }
//
//    public void testNoProperty() throws Exception {
//        // Create a mock server
//        MockWebServer server = new MockWebServer();
//        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
//        server.play();
//
//        // Create our root node using the mock server
//        Hyper root = HyperJson.createRoot(server.getUrl(JsonFiles.ROOT.URL));
//        Task<Hyper> fetchTask = root.fetch();
//        fetchTask.waitForCompletion();
//
//        Task<Object> propertyTask = root.get("banana-nana-fo-fanna");
//        propertyTask.waitForCompletion();
//
//        // test error
//        assertFalse(fetchTask.isFaulted());
//        assertNull(fetchTask.getError());
//        assertNotNull(fetchTask.getResult());
//
//        // test HAS error
//        assertTrue(propertyTask.isFaulted());
//        assertNotNull(propertyTask.getError());
//        assertNull(propertyTask.getResult());
//
//        // test requests
//        assertEquals(1, server.getRequestCount());
//        RecordedRequest request1 = server.takeRequest();
//        assertEquals(JsonFiles.ROOT.URL, request1.getPath());
//
//        // grab results
//        Exception error = propertyTask.getError();
//
//        // test data
//        assertSame("Exception is MissingPropertyException", error.getClass(), MissingPropertyException.class);
//
//        // shut down the server
//        server.shutdown();
//    }
//
//    public void testNoHref() throws Exception {
//        // Create a mock server
//        MockWebServer server = new MockWebServer();
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
//        server.play();
//
//        // Create our root node using the mock server
//        Hyper root = HyperJson.createRoot(server.getUrl(JsonFiles.USER.URL.replace("<user_id>", "1")));
//        Task<Hyper> fetchTask = root.fetch();
//        fetchTask.waitForCompletion();
//
//        assertFalse(fetchTask.isFaulted());
//        assertNull(fetchTask.getError());
//        assertNotNull(fetchTask.getResult());
//
//        Task<Hyper> propertyTask = root.get("avatar.default");
//        propertyTask.waitForCompletion();
//
//        assertFalse(propertyTask.isFaulted());
//        assertNull(propertyTask.getError());
//        assertNotNull(propertyTask.getResult());
//
//        Task<Hyper> fetch2 = propertyTask.getResult().fetch();
//        fetch2.waitForCompletion();
//
//        assertTrue(fetch2.isFaulted());
//        assertNotNull(fetch2.getError());
//        assertNull(fetch2.getResult());
//
//        // test requests
//        assertEquals(1, server.getRequestCount());
//        RecordedRequest request1 = server.takeRequest();
//        assertEquals(JsonFiles.USER.URL.replace("<user_id>", "1"), request1.getPath());
//
//        // grab results
//        Exception error = fetch2.getError();
//
//        // test data
//        assertSame("Exception is NoHrefException", error.getClass(), NoHrefException.class);
//
//        // shut down the server
//        server.shutdown();
//    }
//
//    public void testCollectionItem() throws Exception {
//
//        // Create a mock server
//        MockWebServer server = new MockWebServer();
//        server.enqueue(new MockResponse().setBody(JsonFiles.FRIENDS.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
//        server.play();
//
//        // Create our root node using the mock server
//        Hyper root = HyperJson.createRoot(server.getUrl(JsonFiles.FRIENDS.URL.replace("<user_id>", "1")));
//        Task<Hyper> fetchTask = root.fetch();
//        fetchTask.waitForCompletion();
//
//        assertFalse(fetchTask.isFaulted());
//        assertNull(fetchTask.getError());
//        assertNotNull(fetchTask.getResult());
//
//        Task<Hyper> friendTask = root.get("0");
//        friendTask.waitForCompletion();
//
//        assertFalse(friendTask.isFaulted());
//        assertNull(friendTask.getError());
//        assertNotNull(friendTask.getResult());
//
//        // test requests
//        assertEquals(1, server.getRequestCount());
//        RecordedRequest request1 = server.takeRequest();
//        assertEquals(JsonFiles.FRIENDS.URL.replace("<user_id>", "1"), request1.getPath());
//
//        // grab results
//        Hyper friend = friendTask.getResult();
//
//        // test data
//        assertEquals("/api/users/1", friend.getHref().getPath());
//
//        // shut down the server
//        server.shutdown();
//
//    }
//
//    public void testCollectionItemCached() throws Exception {
//
//        // Create a mock server
//        MockWebServer server = new MockWebServer();
//        server.enqueue(new MockResponse().setBody(JsonFiles.FRIENDS.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
//        server.play();
//
//        // Create our root node using the mock server
//        Hyper root = HyperJson.createRoot(server.getUrl(JsonFiles.FRIENDS.URL.replace("<user_id>", "1")));
//        Task<Hyper> fetchTask = root.fetch();
//        fetchTask.waitForCompletion();
//
//        assertFalse(fetchTask.isFaulted());
//        assertNull(fetchTask.getError());
//        assertNotNull(fetchTask.getResult());
//
//        Task<Hyper> friend1Task = root.get("0");
//        friend1Task.waitForCompletion();
//
//        assertFalse(friend1Task.isFaulted());
//        assertNull(friend1Task.getError());
//        assertNotNull(friend1Task.getResult());
//
//        Task<Hyper> friend2Task = root.get("0");
//        friend2Task.waitForCompletion();
//
//        assertFalse(friend2Task.isFaulted());
//        assertNull(friend2Task.getError());
//        assertNotNull(friend2Task.getResult());
//
//        // test requests
//        assertEquals(1, server.getRequestCount());
//        RecordedRequest request1 = server.takeRequest();
//        assertEquals(JsonFiles.FRIENDS.URL.replace("<user_id>", "1"), request1.getPath());
//
//        // grab results
//        Hyper friend1 = friend1Task.getResult();
//        Hyper friend2 = friend1Task.getResult();
//
//        // test data
//        assertEquals("/api/users/1", friend1.getHref().getPath());
//        assertSame("friend1 == friend2", friend1, friend2);
//
//        // shut down the server
//        server.shutdown();
//
//    }

    public void testDeepCollectionItem() throws Exception {

        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJson.createRoot(server.getUrl(JsonFiles.USER.URL.replace("<user_id>", "1")));
        Task<Hyper> fetchTask = root.fetch();
        fetchTask.waitForCompletion();

        assertFalse(fetchTask.isFaulted());
        assertNull(fetchTask.getError());
        assertNotNull(fetchTask.getResult());

        Task<Hyper> friendTask = root.get("friends.0");
        friendTask.waitForCompletion();

        assertFalse(friendTask.isFaulted());
        assertNull(friendTask.getError());
        assertNotNull(friendTask.getResult());

        // test requests
        assertEquals(1, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.USER.URL.replace("<user_id>", "1"), request1.getPath());

        // grab results
        Hyper friend = friendTask.getResult();

        // test data
        assertEquals("/api/users/1", friend.getHref().getPath());

        // shut down the server
        server.shutdown();

    }

//    public void testDeepCollectionItemCached() throws Exception {
//
//        // Create a mock server
//        MockWebServer server = new MockWebServer();
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
//        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
//        server.play();
//
//        // Create our root node using the mock server
//        Hyper root = HyperJson.createRoot(server.getUrl(JsonFiles.USER.URL.replace("<user_id>", "1")));
//        Task<Hyper> fetchTask = root.fetch();
//        fetchTask.waitForCompletion();
//
//        assertFalse(fetchTask.isFaulted());
//        assertNull(fetchTask.getError());
//        assertNotNull(fetchTask.getResult());
//
//        Task<Hyper> friend1Task = root.get("friends.0");
//        friend1Task.waitForCompletion();
//
//        assertFalse(friend1Task.isFaulted());
//        assertNull(friend1Task.getError());
//        assertNotNull(friend1Task.getResult());
//
//        Task<Hyper> friend2Task = root.get("friends.0");
//        friend2Task.waitForCompletion();
//
//        assertFalse(friend2Task.isFaulted());
//        assertNull(friend2Task.getError());
//        assertNotNull(friend2Task.getResult());
//
//        // test requests
//        assertEquals(1, server.getRequestCount());
//        RecordedRequest request1 = server.takeRequest();
//        assertEquals(JsonFiles.USER.URL.replace("<user_id>", "1"), request1.getPath());
//
//        // grab results
//        Hyper friend1 = friend1Task.getResult();
//        Hyper friend2 = friend1Task.getResult();
//
//        // test data
//        assertEquals("/api/users/1", friend1.getHref().getPath());
//        assertSame("friend1 == friend2", friend1, friend2);
//
//        // shut down the server
//        server.shutdown();
//
//    }


}
