package io.hypergroup.hyper.requests;

import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Wrapper for Response that circumvents multiple body-reads.
 */
public class ResponsePackage {

    /**
     * The Response we are wrapping
     */
    private Response mResponse;

    /**
     * Storage of the response body
     */
    private String mBody;

    /**
     * Wrap a Response
     */
    public ResponsePackage(Response response) {
        mResponse = response;
    }

    /**
     * Safely read the response body multiple times
     *
     * @return The response body
     * @throws IOException If the underlying body retrieval mechanism fails
     */
    public String getBody() throws IOException {
        if (mBody == null) {
            mBody = mResponse.body().string();
        }
        return mBody;
    }

    /**
     * @return Get the wrapped Response object
     */
    public Response getResponse() {
        return mResponse;
    }
}
