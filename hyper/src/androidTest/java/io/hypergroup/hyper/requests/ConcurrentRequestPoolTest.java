package io.hypergroup.hyper.requests;

import android.test.AndroidTestCase;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import java.net.URL;
import java.util.concurrent.Executor;

import bolts.Task;
import io.hypergroup.hyper.context.requests.ConcurrentRequestPool;
import io.hypergroup.hyper.context.requests.ResponsePackage;

public class ConcurrentRequestPoolTest extends AndroidTestCase {

    public ConcurrentRequestPoolTest() {
        super();
    }

    public void testConcurrentReturnsSameTask() throws Exception {
        ConcurrentRequestPool pool = new ConcurrentRequestPool();
        OkHttpClient client = new OkHttpClient();
        Executor exec = Task.BACKGROUND_EXECUTOR;

        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(""));
        server.enqueue(new MockResponse().setBody("")); // extra in case of failure
        server.play();

        URL foo = server.getUrl("/foo");

        Request request1 = new Request.Builder().get().url(foo).build();
        Request request2 = new Request.Builder().get().url(foo).build();

        Task<ResponsePackage> packageTask1 = pool.getResponseTask(client, exec, request1);
        Task<ResponsePackage> packageTask2 = pool.getResponseTask(client, exec, request2);

        assertSame("request tasks should be the same", packageTask1, packageTask2);

        // sanity checks
        packageTask1.waitForCompletion();
        assertNull(packageTask1.getError());
        assertFalse(packageTask1.isFaulted());
        assertNotNull(packageTask1.getResult());

        // sanity checks
        packageTask2.waitForCompletion();
        assertNull(packageTask2.getError());
        assertFalse(packageTask2.isFaulted());
        assertNotNull(packageTask2.getResult());

        server.shutdown();
    }

    public void testSequentialReturnsNewTask() throws Exception {
        ConcurrentRequestPool pool = new ConcurrentRequestPool();
        OkHttpClient client = new OkHttpClient();
        Executor exec = Task.BACKGROUND_EXECUTOR;

        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(""));
        server.enqueue(new MockResponse().setBody("")); // extra in case of failure
        server.play();

        URL foo = server.getUrl("/foo");

        Request request1 = new Request.Builder().get().url(foo).build();
        Request request2 = new Request.Builder().get().url(foo).build();

        Task<ResponsePackage> packageTask1 = pool.getResponseTask(client, exec, request1);
        packageTask1.waitForCompletion();
        Task<ResponsePackage> packageTask2 = pool.getResponseTask(client, exec, request2);
        packageTask2.waitForCompletion();

        assertNotSame("request tasks should be different", packageTask1, packageTask2);

        // sanity checks
        assertNull(packageTask1.getError());
        assertFalse(packageTask1.isFaulted());
        assertNotNull(packageTask1.getResult());

        assertNull(packageTask2.getError());
        assertFalse(packageTask2.isFaulted());
        assertNotNull(packageTask2.getResult());

        server.shutdown();
    }
}
