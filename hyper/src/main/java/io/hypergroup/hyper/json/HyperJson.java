package io.hypergroup.hyper.json;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import io.hypergroup.hyper.Data;
import io.hypergroup.hyper.Hyper;
import io.hypergroup.hyper.context.HyperContext;
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
     * Accept anything at a low priority
     */
    private static final String ACCEPT_ANY = "*/*";

    /**
     * HTTP Header for Accept-Encoding
     */
    private static final String HTTP_HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * Allow gzip and compression
     */
    private static final String HTTP_HEADER_ACCEPT_ENCODING_GZIP = "compress, gzip";

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
            .addHeader(HTTP_HEADER_ACCEPT_ENCODING, HTTP_HEADER_ACCEPT_ENCODING_GZIP) // accept compression
            .addHeader(HEADER_ACCEPT, ACCEPT_ANY)// accept anything really, if parsing fails, then we have problems
            .build(); // build that request
    }

    @Override
    protected Data parseResponse(Response response) throws IOException, DataParseException {
        // get the body from the response
        String body = response.body().string();
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
