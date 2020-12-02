package org.mockserver.netty.integration.mock.initializer;

import org.mockserver.mock.Expectation;
import org.mockserver.server.initialize.ExpectationInitializer;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * @author jamesdbloom
 */
public class ExpectationInitializerIntegrationExample implements ExpectationInitializer {
    @Override
    public Expectation[] initializeExpectations() {
        return new Expectation[]{
            new Expectation(
                request()
                    .withPath("/simpleFirst")
            )
                .thenRespond(
                response()
                    .withBody("some first response")
            ),
            new Expectation(
                request()
                    .withPath("/simpleSecond")
            )
                .thenRespond(
                response()
                    .withBody("some second response")
            )
        };
    }
}
