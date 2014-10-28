package io.hypergroup.hyper.cache;

import android.test.AndroidTestCase;

import java.net.URL;

import io.hypergroup.hyper.Hyper;
import io.hypergroup.hyper.context.HyperContext;
import io.hypergroup.hyper.context.cache.HyperCache;
import io.hypergroup.hyper.json.HyperJson;

public class HyperCacheTest extends AndroidTestCase {

    public HyperCacheTest() {
        super();
    }

    public void testEmpty() throws Exception {
        HyperCache cache = new HyperCache();

        Hyper result = cache.get("foo.bar.baz.whizpop");
        assertNull(result);
    }

    public void testFull() throws Exception {
        HyperCache cache = new HyperCache();
        HyperContext.Builder builder = new HyperContext.Builder();
        builder.setHyperCache(cache);
        Hyper root = HyperJson.createRoot(new URL("http://example.com"), builder.build());

        Hyper result = cache.get(root.getKeyPath());
        assertSame("cache get == root", root, result);
    }

    public void testMiss() throws Exception {
        HyperCache cache = new HyperCache();
        HyperContext.Builder builder = new HyperContext.Builder();
        builder.setHyperCache(cache);
        Hyper root = HyperJson.createRoot(new URL("http://example.com"), builder.build());

        Hyper result = cache.get("foo.bar.baz.whizpop");
        assertNull(result);
    }
}
