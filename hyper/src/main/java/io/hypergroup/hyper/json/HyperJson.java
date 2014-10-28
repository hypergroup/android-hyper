package io.hypergroup.hyper.json;

import com.squareup.okhttp.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import io.hypergroup.hyper.Data;
import io.hypergroup.hyper.Hyper;
import io.hypergroup.hyper.context.HyperContext;
import io.hypergroup.hyper.context.requests.ResponsePackage;
import io.hypergroup.hyper.exception.DataParseException;

/**
 * hyper+json implementation of hypermedia.
 * <br/>
 * Hyper root = HyperJson.createRoot("http://example.com/api/v1/root");
 * <br/>
 * ...boom
 */
public class HyperJson extends Hyper {

    /**
     * HTTP Header for Accept
     */
    private static final String HEADER_ACCEPT = "Accept";

    /**
     * Accept application/hyper+json at top priority
     */
    private static final String ACCEPT_HYPER_JSON = "application/hyper+json; q=1";

    /**
     * Accept application/json at a high priority
     */
    private static final String ACCEPT_JSON = "application/json; q=0.95";

    /**
     * Accept text at a low priority
     */
    private static final String ACCEPT_TEXT = "text/plain; q=0.1";

    /**
     * Accept anything at a low priority
     */
    private static final String ACCEPT_ANY = "*/*; q=0";

    /**
     * Create a new root HyperJson node at the given URL
     *
     * @param url URL that is the root of the hypermedia
     * @return The newly created root Hyper node
     */
    public static Hyper createRoot(URL url) {
        // bare bones root node
        return createRoot(url, (new HyperContext.Builder()).build());
    }

    /**
     * Create a new root HyperJson node at the given url with your http client
     *
     * @param url          URL that is the root of the hypermedia
     * @param hyperContext Your configured HyperContext
     * @return The newly created root Hyper node
     */
    public static Hyper createRoot(URL url, HyperContext hyperContext) {
        // Build context
        // create the root node
        HyperJson node = new HyperJson(null, url, hyperContext);
        // don't forget to set the root
        hyperContext.setRoot(node);
        return node;
    }

    /* default */ HyperJson(String keyPath, URL href, HyperContext context) {
        super(keyPath, href, context);
    }

    /* default */ HyperJson(String keyPath, URL relativeHref, Data data, HyperContext context) {
        super(keyPath, relativeHref, data, context);
    }

    @Override
    protected Request buildRequest(URL href) {
        return new Request.Builder()
                .url(mHref)
                .addHeader(HEADER_ACCEPT, ACCEPT_HYPER_JSON)// accept and prefer hyper+json
                .addHeader(HEADER_ACCEPT, ACCEPT_JSON)// accept json
                .addHeader(HEADER_ACCEPT, ACCEPT_TEXT)// accept text also
                .addHeader(HEADER_ACCEPT, ACCEPT_ANY)// accept anything really, if parsing fails, then we have problems
                .build(); // build that request
    }

    @Override
    protected Data parseResponse(ResponsePackage response) throws IOException, DataParseException {
        // get the body from the response
        String body = response.getBody();
        // try to parse the response
        JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException ex) {
            // fail sometimes
            throw new DataParseException("Error parsing JSON", ex);
        }
        // wrap JSONObject with the Data interface
        return new JsonData(json);
    }

    @Override
    protected Hyper createHyperNodeFromData(String keyPath, URL relativeHref, Data data) {
        return new HyperJson(keyPath, relativeHref, data, getContext());
    }

    @Override
    protected boolean isRawData(Object value) {
        return value instanceof JSONObject;
    }

    @Override
    protected Data createDataFromRawData(Object raw) {
        return new JsonData((JSONObject) raw);
    }

}
