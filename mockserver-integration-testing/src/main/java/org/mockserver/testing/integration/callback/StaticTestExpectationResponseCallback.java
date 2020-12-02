package org.mockserver.testing.integration.callback;

import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.util.List;
import java.util.Vector;

import static org.mockserver.model.HttpResponse.response;

/**
 * @author jamesdbloom
 */
public class StaticTestExpectationResponseCallback implements ExpectationResponseCallback {

    public static final List<HttpRequest> httpRequests = new Vector<HttpRequest>();
    public static HttpResponse httpResponse = response();

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        httpRequests.add(httpRequest);
        return httpResponse;
    }
}
