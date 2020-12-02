package org.mockserver.examples.mockserver;

import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.ClearType;
import org.mockserver.model.RequestDefinition;
import org.mockserver.verify.VerificationTimes;

import java.util.List;

import static org.mockserver.model.HttpRequest.request;

public class ProxyClientExamples {

    public void verifyRequests() {
        new MockServerClient("localhost", 1080)
            .verify(
                request()
                    .withPath("/some/path"),
                VerificationTimes.atLeast(2)
            );
    }

    public void verifyRequestsClientAndProxy() {
        new ClientAndServer(1080)
            .verify(
                request()
                    .withPath("/some/path"),
                VerificationTimes.atLeast(2)
            );
    }

    public void verifyRequestSequence() {
        new MockServerClient("localhost", 1080)
            .verify(
                request()
                    .withPath("/some/path/one"),
                request()
                    .withPath("/some/path/two"),
                request()
                    .withPath("/some/path/three")
            );
    }

    public void retrieveRecordedRequests() {
        RequestDefinition[] recordedRequests = new MockServerClient("localhost", 1080)
            .retrieveRecordedRequests(
                request()
                    .withPath("/some/path")
                    .withMethod("POST")
            );
    }

    public void retrieveRecordedLogMessages() {
        String[] logMessages = new MockServerClient("localhost", 1080)
            .retrieveLogMessagesArray(
                request()
                    .withPath("/some/path")
                    .withMethod("POST")
            );
    }

    public void clear() {
        new MockServerClient("localhost", 1080).clear(
            request()
                .withPath("/some/path")
                .withMethod("POST")
        );
    }

    public void clearLogs() {
        new MockServerClient("localhost", 1080).clear(
            request()
                .withPath("/some/path")
                .withMethod("POST"),
            ClearType.LOG
        );
    }

    public void reset() {
        new MockServerClient("localhost", 1080).reset();
    }

    public void bindToAdditionFreePort() {
        List<Integer> boundPorts = new MockServerClient("localhost", 1080).bind(
            0
        );
    }

    public void bindToAdditionalSpecifiedPort() {
        List<Integer> boundPorts = new MockServerClient("localhost", 1080).bind(
            1081, 1082
        );
    }

}
