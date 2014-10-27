package io.hypergroup.hyper;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.TestCase;

import bolts.Continuation;
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

    public void testRequestHeaders() throws Exception {

        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
        server.play();

        // Create our hyper node using the mock server
        Hyper hyper = HyperJson.createRoot(server.getUrl("/api"));

        // Perform a fetch for testing
        Task<Hyper> task = hyper.fetch();
        task.waitForCompletion();

        // test error
        assertFalse(task.isFaulted());
        assertNull(task.getError());

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
        Hyper root = HyperJson.createRoot(server.getUrl("/api"));

        // Perform a fetch for testing
        Task<Hyper> task = root.fetch();
        task.waitForCompletion();

        // test error
        assertFalse(task.isFaulted());
        assertNull(task.getError());

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
        Hyper root = HyperJson.createRoot(server.getUrl("/api"));

        // Perform a fetch for testing
        Task<String> task = root.get("current_user.first_name");
        task.waitForCompletion();

        // test error
        assertFalse(task.isFaulted());
        assertNull(task.getError());

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
        Hyper root = HyperJson.createRoot(server.getUrl("/api"));

        // Perform a fetch for testing
        Task<Integer> task = root.get("current_user.friends.count");
        task.waitForCompletion();

        // test error
        assertFalse(task.isFaulted());
        assertNull(task.getError());

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

    public void testDeepNodeIsCached() throws Exception {

        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJson.createRoot(server.getUrl("/api"));

        // Perform a fetch for testing
        Task<Hyper> fetchUserTask1 = root.<Hyper>get("current_user").continueWithTask(new Continuation<Hyper, Task<Hyper>>() {
            @Override
            public Task<Hyper> then(Task<Hyper> task) throws Exception {
                return task.getResult().fetch();
            }
        });
        fetchUserTask1.waitForCompletion();

        // fetch the same object for testing
        Task<Hyper> fetchUserTask2 = root.<Hyper>get("current_user").continueWithTask(new Continuation<Hyper, Task<Hyper>>() {
            @Override
            public Task<Hyper> then(Task<Hyper> task) throws Exception {
                return task.getResult().fetch();
            }
        });
        fetchUserTask2.waitForCompletion();

        // perform tasks in parallel (2nd task initiated during first 1st)
        fetchUserTask1.waitForCompletion();
        fetchUserTask2.waitForCompletion();

        // test error
        assertFalse(fetchUserTask1.isFaulted());
        assertNull(fetchUserTask1.getError());
        assertFalse(fetchUserTask2.isFaulted());
        assertNull(fetchUserTask2.getError());

        // grab results
        Hyper user1 = fetchUserTask1.getResult();
        Hyper user2 = fetchUserTask2.getResult();

        // test requests
        assertEquals(2, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.ROOT.URL, request1.getPath());
        RecordedRequest request2 = server.takeRequest();
        assertEquals(JsonFiles.USER.URL.replace("<user_id>", "1"), request2.getPath());

        // test data
        assertTrue("user1 == user2", user1 == user2);

        // shut down the server
        server.shutdown();
    }

    public void testDeepNodeIsCachedParallel() throws Exception {

        // Create a mock server
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(JsonFiles.ROOT.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET));
        server.enqueue(new MockResponse().setBody(JsonFiles.USER.GET)); // extra in case of failure
        server.play();

        // Create our root node using the mock server
        Hyper root = HyperJson.createRoot(server.getUrl("/api"));

        // Perform a fetch for testing
        Task<Hyper> fetchUserTask1 = root.<Hyper>get("current_user").continueWithTask(new Continuation<Hyper, Task<Hyper>>() {
            @Override
            public Task<Hyper> then(Task<Hyper> task) throws Exception {
                return task.getResult().fetch();
            }
        });

        // fetch the same object for testing
        Task<Hyper> fetchUserTask2 = root.<Hyper>get("current_user").continueWithTask(new Continuation<Hyper, Task<Hyper>>() {
            @Override
            public Task<Hyper> then(Task<Hyper> task) throws Exception {
                return task.getResult().fetch();
            }
        });

        // perform tasks in parallel (2nd task initiated during first 1st)
        fetchUserTask1.waitForCompletion();
        fetchUserTask2.waitForCompletion();

        // test error
        assertFalse(fetchUserTask1.isFaulted());
        assertNull(fetchUserTask1.getError());
        assertFalse(fetchUserTask2.isFaulted());
        assertNull(fetchUserTask2.getError());

        // grab results
        Hyper user1 = fetchUserTask1.getResult();
        Hyper user2 = fetchUserTask2.getResult();

        // test requests
        assertEquals(2, server.getRequestCount());
        RecordedRequest request1 = server.takeRequest();
        assertEquals(JsonFiles.ROOT.URL, request1.getPath());
        RecordedRequest request2 = server.takeRequest();
        assertEquals(JsonFiles.USER.URL.replace("<user_id>", "1"), request2.getPath());

        // test data
        assertTrue("user1 == user2", user1 == user2);

        // shut down the server
        server.shutdown();
    }
}
