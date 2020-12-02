package org.mockserver.log;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.log.model.LogEntry;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.matchers.TimeToLive;
import org.mockserver.matchers.Times;
import org.mockserver.mock.Expectation;
import org.mockserver.mock.HttpState;
import org.mockserver.model.RequestDefinition;
import org.mockserver.scheduler.Scheduler;
import org.slf4j.event.Level;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockserver.log.model.LogEntry.LogMessageType.*;
import static org.mockserver.log.model.LogEntryMessages.RECEIVED_REQUEST_MESSAGE_FORMAT;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.notFoundResponse;
import static org.mockserver.model.HttpResponse.response;
import static org.slf4j.event.Level.INFO;

public class MockServerEventLogTest {

    private MockServerLogger mockServerLogger;
    private MockServerEventLog mockServerEventLog;

    @BeforeClass
    public static void fixTime() {
        TimeService.fixedTime = true;
    }

    @Before
    public void setupTestFixture() {
        Scheduler scheduler = mock(Scheduler.class);
        HttpState httpStateHandler = new HttpState(new MockServerLogger(), scheduler);
        mockServerLogger = httpStateHandler.getMockServerLogger();
        mockServerEventLog = httpStateHandler.getMockServerLog();
    }

    private List<LogEntry> retrieveMessageLogEntries(RequestDefinition httpRequest) {
        CompletableFuture<List<LogEntry>> future = new CompletableFuture<>();
        mockServerEventLog.retrieveMessageLogEntries(httpRequest, future::complete);
        try {
            return future.get(60, SECONDS);
        } catch (Exception e) {
            fail(e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private List<LogEntry> retrieveMessageLogEntriesIncludingDeleted(RequestDefinition httpRequest) {
        CompletableFuture<List<LogEntry>> future = new CompletableFuture<>();
        mockServerEventLog.retrieveMessageLogEntriesIncludingDeleted(httpRequest, future::complete);
        try {
            return future.get(60, SECONDS);
        } catch (Exception e) {
            fail(e.getMessage());
            return null;
        }
    }

    private List<RequestDefinition> retrieveRequests(RequestDefinition httpRequest) {
        CompletableFuture<List<RequestDefinition>> result = new CompletableFuture<>();
        mockServerEventLog.retrieveRequests(httpRequest, result::complete);
        try {
            return result.get(60, SECONDS);
        } catch (Exception e) {
            fail(e.getMessage());
            return null;
        }
    }

    private List<LogEntry> retrieveRequestLogEntries() {
        CompletableFuture<List<LogEntry>> future = new CompletableFuture<>();
        mockServerEventLog.retrieveRequestLogEntries(null, future::complete);
        try {
            return future.get(60, SECONDS);
        } catch (Exception e) {
            fail(e.getMessage());
            return null;
        }
    }

    private List<LogEntry> retrieveRequestResponseMessageLogEntries(RequestDefinition httpRequest) {
        CompletableFuture<List<LogEntry>> future = new CompletableFuture<>();
        mockServerEventLog.retrieveRequestResponseMessageLogEntries(httpRequest, future::complete);
        try {
            return future.get(60, SECONDS);
        } catch (Exception e) {
            fail(e.getMessage());
            return null;
        }
    }

    private List<Expectation> retrieveRecordedExpectations(RequestDefinition httpRequest) {
        CompletableFuture<List<Expectation>> future = new CompletableFuture<>();
        mockServerEventLog.retrieveRecordedExpectations(httpRequest, future::complete);
        try {
            return future.get(60, SECONDS);
        } catch (Exception e) {
            fail(e.getMessage());
            return null;
        }
    }

    @Test
    public void shouldRetrieveLogEntriesContainingNulls() {
        Level originalLevel = ConfigurationProperties.logLevel();
        try {
            // given
            ConfigurationProperties.logLevel("INFO");
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(RECEIVED_REQUEST)
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(NO_MATCH_RESPONSE)
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(RECEIVED_REQUEST)
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_MATCHED)
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_RESPONSE)
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(TRACE)
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(FORWARDED_REQUEST)
            );

            // then
            assertThat(retrieveRequests(null), empty());
            assertThat(retrieveRequestResponseMessageLogEntries(null), contains(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(NO_MATCH_RESPONSE)
                    .setHttpRequests(new RequestDefinition[0]),
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(EXPECTATION_RESPONSE)
                    .setHttpRequests(new RequestDefinition[0]),
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(FORWARDED_REQUEST)
                    .setHttpRequests(new RequestDefinition[0])
            ));
            assertThat(retrieveRecordedExpectations(null), empty());
        } finally {
            ConfigurationProperties.logLevel(originalLevel.name());
        }
    }

    @Test
    public void shouldRetrieveLogEntriesWithNullRequestMatcher() {
        Level originalLevel = ConfigurationProperties.logLevel();
        try {
            // given
            ConfigurationProperties.logLevel("INFO");
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(RECEIVED_REQUEST)
                    .setHttpRequest(request("request_one"))
                    .setMessageFormat(RECEIVED_REQUEST_MESSAGE_FORMAT)
                    .setArguments(request("request_one"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(NO_MATCH_RESPONSE)
                    .setHttpRequest(request("request_one"))
                    .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                    .setMessageFormat("no expectation for:{}returning response:{}")
                    .setArguments(request("request_one"), notFoundResponse())
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(RECEIVED_REQUEST)
                    .setHttpRequests(new RequestDefinition[]{request("request_two"), request("request_three")})
                    .setMessageFormat(RECEIVED_REQUEST_MESSAGE_FORMAT)
                    .setArguments(request("request_two"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_MATCHED)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setExpectation(new Expectation(request("request_two")).thenRespond(response("response_two")))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setArguments(request("request_two"), new Expectation(request("request_two")).thenRespond(response("response_two")))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_RESPONSE)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setHttpResponse(response("response_two"))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setMessageFormat("returning response:{}for request:{}for action:{}")
                    .setArguments(request("request_two"), response("response_two"), response("response_two"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(TRACE)
                    .setHttpRequest(request("request_four"))
                    .setExpectation(new Expectation(request("request_four")).thenRespond(response("response_four")))
                    .setMessageFormat("some random{}message")
                    .setArguments("argument_one")
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(FORWARDED_REQUEST)
                    .setHttpRequest(request("request_five"))
                    .setHttpResponse(response("response_five"))
                    .setExpectation(request("request_five"), response("response_five"))
            );

            // then
            assertThat(retrieveRequests(null), contains(
                request("request_one"),
                request("request_two"),
                request("request_three")
            ));
            assertThat(retrieveRequestResponseMessageLogEntries(null), contains(
                new LogEntry()
                    .setEpochTime(TimeService.currentTimeMillis())
                    .setLogLevel(INFO)
                    .setType(NO_MATCH_RESPONSE)
                    .setHttpRequest(request("request_one"))
                    .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                    .setMessageFormat("no expectation for:{}returning response:{}")
                    .setArguments(request("request_one"), notFoundResponse()),
                new LogEntry()
                    .setEpochTime(TimeService.currentTimeMillis())
                    .setLogLevel(INFO)
                    .setType(EXPECTATION_RESPONSE)
                    .setHttpRequest(request("request_two"))
                    .setHttpResponse(response("response_two"))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setMessageFormat("returning response:{}for request:{}for action:{}")
                    .setArguments(request("request_two"), response("response_two"), response("response_two")),
                new LogEntry()
                    .setType(FORWARDED_REQUEST)
                    .setHttpRequest(request("request_five"))
                    .setHttpResponse(response("response_five"))
                    .setExpectation(request("request_five"), response("response_five"))
            ));
            assertThat(retrieveRecordedExpectations(null), contains(
                new Expectation(request("request_five"), Times.once(), TimeToLive.unlimited(), 0).thenRespond(response("response_five"))
            ));
            List<LogEntry> actual = retrieveMessageLogEntries(null);
            assertThat(actual, contains(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(RECEIVED_REQUEST)
                    .setHttpRequest(request("request_one"))
                    .setMessageFormat(RECEIVED_REQUEST_MESSAGE_FORMAT)
                    .setArguments(request("request_one")),
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(NO_MATCH_RESPONSE)
                    .setHttpRequest(request("request_one"))
                    .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                    .setMessageFormat("no expectation for:{}returning response:{}")
                    .setArguments(request("request_one"), notFoundResponse()),
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(RECEIVED_REQUEST)
                    .setHttpRequests(new RequestDefinition[]{request("request_two"), request("request_three")})
                    .setMessageFormat(RECEIVED_REQUEST_MESSAGE_FORMAT)
                    .setArguments(request("request_two")),
                new LogEntry()
                    .setType(EXPECTATION_MATCHED)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setExpectation(new Expectation(request("request_two")).thenRespond(response("response_two")))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setArguments(request("request_two"), new Expectation(request("request_two")).thenRespond(response("response_two"))),
                new LogEntry()
                    .setType(EXPECTATION_RESPONSE)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setHttpResponse(response("response_two"))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setMessageFormat("returning response:{}for request:{}for action:{}")
                    .setArguments(request("request_two"), response("response_two"), response("response_two")),
                new LogEntry()
                    .setType(TRACE)
                    .setHttpRequest(request("request_four"))
                    .setExpectation(new Expectation(request("request_four")).thenRespond(response("response_four")))
                    .setMessageFormat("some random{}message")
                    .setArguments("argument_one"),
                new LogEntry()
                    .setType(FORWARDED_REQUEST)
                    .setHttpRequest(request("request_five"))
                    .setHttpResponse(response("response_five"))
                    .setExpectation(request("request_five"), response("response_five"))
            ));
        } finally {
            ConfigurationProperties.logLevel(originalLevel.name());
        }
    }

    @Test
    public void shouldRetrieveLogEntriesWithRequestMatcher() {
        Level originalLevel = ConfigurationProperties.logLevel();
        try {
            // given
            ConfigurationProperties.logLevel("INFO");
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(RECEIVED_REQUEST)
                    .setHttpRequest(request("request_one"))
                    .setMessageFormat(RECEIVED_REQUEST_MESSAGE_FORMAT)
                    .setArguments(request("request_one"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(NO_MATCH_RESPONSE)
                    .setHttpRequest(request("request_one"))
                    .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                    .setMessageFormat("no expectation for:{}returning response:{}")
                    .setArguments(request("request_one"), notFoundResponse())
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(RECEIVED_REQUEST)
                    .setHttpRequest(request("request_two"))
                    .setMessageFormat(RECEIVED_REQUEST_MESSAGE_FORMAT)
                    .setArguments(request("request_two"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_MATCHED)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setExpectation(new Expectation(request("request_two")).thenRespond(response("response_two")))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setArguments(request("request_two"), new Expectation(request("request_two")).thenRespond(response("response_two")))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_RESPONSE)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setHttpResponse(response("response_two"))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setMessageFormat("returning response:{}for request:{}for action:{}")
                    .setArguments(request("request_two"), response("response_two"), response("response_two"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(TRACE)
                    .setHttpRequest(request("request_four"))
                    .setExpectation(new Expectation(request("request_four")).thenRespond(response("response_four")))
                    .setMessageFormat("some random{}message")
                    .setArguments("argument_one")
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(FORWARDED_REQUEST)
                    .setHttpRequest(request("request_five"))
                    .setHttpResponse(response("response_five"))
                    .setExpectation(request("request_five"), response("response_five"))
            );

            // then
            RequestDefinition requestMatcher = request("request_one");
            assertThat(retrieveRequests(requestMatcher), contains(
                request("request_one")
            ));
            assertThat(retrieveRequestResponseMessageLogEntries(requestMatcher), contains(
                new LogEntry()
                    .setEpochTime(TimeService.currentTimeMillis())
                    .setLogLevel(INFO)
                    .setType(NO_MATCH_RESPONSE)
                    .setHttpRequest(request("request_one"))
                    .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                    .setMessageFormat("no expectation for:{}returning response:{}")
                    .setArguments(request("request_one"), notFoundResponse())
            ));
            assertThat(retrieveRecordedExpectations(requestMatcher), empty());
            assertThat(retrieveMessageLogEntries(requestMatcher), contains(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(RECEIVED_REQUEST)
                    .setHttpRequest(request("request_one"))
                    .setMessageFormat(RECEIVED_REQUEST_MESSAGE_FORMAT)
                    .setArguments(request("request_one")),
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(NO_MATCH_RESPONSE)
                    .setHttpRequest(request("request_one"))
                    .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                    .setMessageFormat("no expectation for:{}returning response:{}")
                    .setArguments(request("request_one"), notFoundResponse())
            ));
        } finally {
            ConfigurationProperties.logLevel(originalLevel.name());
        }
    }

    @Test
    public void shouldClearWithNullRequestMatcher() {
        Level originalLevel = ConfigurationProperties.logLevel();
        try {
            // given
            ConfigurationProperties.logLevel("INFO");
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(NO_MATCH_RESPONSE)
                    .setHttpRequest(request("request_one"))
                    .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                    .setMessageFormat("no expectation for:{}returning response:{}")
                    .setArguments(request("request_one"), notFoundResponse())
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(EXPECTATION_RESPONSE)
                    .setHttpRequest(request("request_two"))
                    .setHttpResponse(response("response_two"))
                    .setMessageFormat("returning error:{}for request:{}for action:{}")
                    .setArguments(request("request_two"), response("response_two"), response("response_two"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_MATCHED)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_one"))
                    .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setArguments(request("request_one"), new Expectation(request("request_one")).thenRespond(response("response_two")))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_MATCHED)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setExpectation(new Expectation(request("request_two")).thenRespond(response("response_two")))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setArguments(request("request_two"), new Expectation(request("request_two")).thenRespond(response("response_two")))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(TRACE)
                    .setHttpRequest(request("request_four"))
                    .setExpectation(new Expectation(request("request_four")).thenRespond(response("response_four")))
                    .setMessageFormat("some random{}message")
                    .setArguments("argument_one")
            );

            // when
            mockServerEventLog.clear(null);

            // then
            assertThat(retrieveRequests(null), empty());
            assertThat(retrieveRecordedExpectations(null), empty());
            assertThat(retrieveMessageLogEntries(null), contains(new LogEntry()
                .setType(CLEARED)
                .setHttpRequest(request())
                .setMessageFormat("cleared logs that match:{}")
                .setArguments("{}")));
            assertThat(retrieveRequestLogEntries(), empty());
            assertThat(retrieveRequestResponseMessageLogEntries(null), empty());
        } finally {
            ConfigurationProperties.logLevel(originalLevel.name());
        }
    }

    @Test
    public void shouldClearWithNullRequestMatcherWhenWhenLogLevelDebug() {
        Level originalLevel = ConfigurationProperties.logLevel();
        try {
            // given
            ConfigurationProperties.logLevel("DEBUG");
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(NO_MATCH_RESPONSE)
                    .setHttpRequest(request("request_one"))
                    .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                    .setMessageFormat("no expectation for:{}returning response:{}")
                    .setArguments(request("request_one"), notFoundResponse())
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(EXPECTATION_RESPONSE)
                    .setHttpRequest(request("request_two"))
                    .setHttpResponse(response("response_two"))
                    .setMessageFormat("returning error:{}for request:{}for action:{}")
                    .setArguments(request("request_two"), response("response_two"), response("response_two"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_MATCHED)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_one"))
                    .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setArguments(request("request_one"), new Expectation(request("request_one")).thenRespond(response("response_two")))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_MATCHED)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setExpectation(new Expectation(request("request_two")).thenRespond(response("response_two")))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setArguments(request("request_two"), new Expectation(request("request_two")).thenRespond(response("response_two")))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(TRACE)
                    .setHttpRequest(request("request_four"))
                    .setExpectation(new Expectation(request("request_four")).thenRespond(response("response_four")))
                    .setMessageFormat("some random{}message")
                    .setArguments("argument_one")
            );

            // when
            mockServerEventLog.clear(null);

            // then
            assertThat(retrieveRequests(null), empty());
            assertThat(retrieveRecordedExpectations(null), empty());
            List<LogEntry> actual = Objects.requireNonNull(retrieveMessageLogEntriesIncludingDeleted(null));
            assertThat(actual.get(0), is(new LogEntry()
                .setDeleted(true)
                .setLogLevel(INFO)
                .setType(NO_MATCH_RESPONSE)
                .setHttpRequest(request("request_one"))
                .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                .setMessageFormat("no expectation for:{}returning response:{}")
                .setArguments(request("request_one"), notFoundResponse())));
            assertThat(actual.get(1), is(new LogEntry()
                .setDeleted(true)
                .setLogLevel(INFO)
                .setType(EXPECTATION_RESPONSE)
                .setHttpRequest(request("request_two"))
                .setHttpResponse(response("response_two"))
                .setMessageFormat("returning error:{}for request:{}for action:{}")
                .setArguments(request("request_two"), response("response_two"), response("response_two"))));
            assertThat(actual.get(2), is(new LogEntry()
                .setDeleted(true)
                .setType(EXPECTATION_MATCHED)
                .setLogLevel(INFO)
                .setHttpRequest(request("request_one"))
                .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                .setMessageFormat("request:{}matched expectation:{}")
                .setArguments(request("request_one"), new Expectation(request("request_one")).thenRespond(response("response_two")))));
            assertThat(actual.get(3), is(new LogEntry()
                .setDeleted(true)
                .setType(EXPECTATION_MATCHED)
                .setLogLevel(INFO)
                .setHttpRequest(request("request_two"))
                .setExpectation(new Expectation(request("request_two")).thenRespond(response("response_two")))
                .setMessageFormat("request:{}matched expectation:{}")
                .setArguments(request("request_two"), new Expectation(request("request_two")).thenRespond(response("response_two")))));
            assertThat(actual.get(4), is(new LogEntry()
                .setDeleted(true)
                .setType(TRACE)
                .setHttpRequest(request("request_four"))
                .setExpectation(new Expectation(request("request_four")).thenRespond(response("response_four")))
                .setMessageFormat("some random{}message")
                .setArguments("argument_one")));
            assertThat(actual.get(5), is(new LogEntry()
                .setType(CLEARED)
                .setHttpRequest(request())
                .setMessageFormat("cleared logs that match:{}")
                .setArguments("{}")));
            assertThat(retrieveRequestLogEntries(), empty());
            assertThat(retrieveRequestResponseMessageLogEntries(null), empty());
        } finally {
            ConfigurationProperties.logLevel(originalLevel.name());
        }
    }

    @Test
    public void shouldClearWithRequestMatcher() {
        Level originalLevel = ConfigurationProperties.logLevel();
        try {
            // given
            ConfigurationProperties.logLevel("INFO");
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(RECEIVED_REQUEST)
                    .setHttpRequest(request("request_one"))
                    .setMessageFormat(RECEIVED_REQUEST_MESSAGE_FORMAT)
                    .setArguments(request("request_one"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(NO_MATCH_RESPONSE)
                    .setHttpRequest(request("request_one"))
                    .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                    .setMessageFormat("no expectation for:{}returning response:{}")
                    .setArguments(request("request_one"), notFoundResponse())
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(RECEIVED_REQUEST)
                    .setHttpRequest(request("request_two"))
                    .setMessageFormat(RECEIVED_REQUEST_MESSAGE_FORMAT)
                    .setArguments(request("request_two"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_MATCHED)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setExpectation(new Expectation(request("request_two")).thenRespond(response("response_two")))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setArguments(request("request_two"), new Expectation(request("request_two")).thenRespond(response("response_two")))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_RESPONSE)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setHttpResponse(response("response_two"))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setMessageFormat("returning response:{}for request:{}for action:{}")
                    .setArguments(request("request_two"), response("response_two"), response("response_two"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(TRACE)
                    .setHttpRequest(request("request_four"))
                    .setExpectation(new Expectation(request("request_four")).thenRespond(response("response_four")))
                    .setMessageFormat("some random{}message")
                    .setArguments("argument_one")
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(FORWARDED_REQUEST)
                    .setHttpRequest(request("request_five"))
                    .setHttpResponse(response("response_five"))
                    .setExpectation(request("request_five"), response("response_five"))
            );

            // when
            mockServerEventLog.clear(request("request_one"));

            // then
            assertThat(retrieveRequests(null), contains(
                request("request_two")
            ));
            assertThat(retrieveRequestResponseMessageLogEntries(null), contains(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(EXPECTATION_RESPONSE)
                    .setHttpRequest(request("request_two"))
                    .setHttpResponse(response("response_two"))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setMessageFormat("returning response:{}for request:{}for action:{}")
                    .setArguments(request("request_two"), response("response_two"), response("response_two")),
                new LogEntry()
                    .setType(FORWARDED_REQUEST)
                    .setHttpRequest(request("request_five"))
                    .setHttpResponse(response("response_five"))
                    .setExpectation(request("request_five"), response("response_five"))
            ));
            assertThat(retrieveRecordedExpectations(null), contains(
                new Expectation(request("request_five"), Times.once(), TimeToLive.unlimited(), 0).thenRespond(response("response_five"))
            ));
            assertThat(retrieveMessageLogEntries(null), contains(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(RECEIVED_REQUEST)
                    .setHttpRequest(request("request_two"))
                    .setMessageFormat(RECEIVED_REQUEST_MESSAGE_FORMAT)
                    .setArguments(request("request_two")),
                new LogEntry()
                    .setType(EXPECTATION_MATCHED)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setExpectation(new Expectation(request("request_two")).thenRespond(response("response_two")))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setArguments(request("request_two"), new Expectation(request("request_two")).thenRespond(response("response_two"))),
                new LogEntry()
                    .setType(EXPECTATION_RESPONSE)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setHttpResponse(response("response_two"))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setMessageFormat("returning response:{}for request:{}for action:{}")
                    .setArguments(request("request_two"), response("response_two"), response("response_two")),
                new LogEntry()
                    .setType(TRACE)
                    .setHttpRequest(request("request_four"))
                    .setExpectation(new Expectation(request("request_four")).thenRespond(response("response_four")))
                    .setMessageFormat("some random{}message")
                    .setArguments("argument_one"),
                new LogEntry()
                    .setType(FORWARDED_REQUEST)
                    .setHttpRequest(request("request_five"))
                    .setHttpResponse(response("response_five"))
                    .setExpectation(request("request_five"), response("response_five")),
                new LogEntry()
                    .setType(CLEARED)
                    .setHttpRequest(request("request_one"))
                    .setMessageFormat("cleared logs that match:{}")
                    .setArguments(request("request_one"))
            ));
        } finally {
            ConfigurationProperties.logLevel(originalLevel.name());
        }
    }

    @Test
    public void shouldClearWithRequestMatcherWhenLogLevelDebug() {
        Level originalLevel = ConfigurationProperties.logLevel();
        try {
            // given
            ConfigurationProperties.logLevel("DEBUG");
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(RECEIVED_REQUEST)
                    .setHttpRequest(request("request_one"))
                    .setMessageFormat(RECEIVED_REQUEST_MESSAGE_FORMAT)
                    .setArguments(request("request_one"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(NO_MATCH_RESPONSE)
                    .setHttpRequest(request("request_one"))
                    .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                    .setMessageFormat("no expectation for:{}returning response:{}")
                    .setArguments(request("request_one"), notFoundResponse())
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(RECEIVED_REQUEST)
                    .setHttpRequest(request("request_two"))
                    .setMessageFormat(RECEIVED_REQUEST_MESSAGE_FORMAT)
                    .setArguments(request("request_two"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_MATCHED)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setExpectation(new Expectation(request("request_two")).thenRespond(response("response_two")))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setArguments(request("request_two"), new Expectation(request("request_two")).thenRespond(response("response_two")))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_RESPONSE)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setHttpResponse(response("response_two"))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setMessageFormat("returning response:{}for request:{}for action:{}")
                    .setArguments(request("request_two"), response("response_two"), response("response_two"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(TRACE)
                    .setHttpRequest(request("request_four"))
                    .setExpectation(new Expectation(request("request_four")).thenRespond(response("response_four")))
                    .setMessageFormat("some random{}message")
                    .setArguments("argument_one")
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(FORWARDED_REQUEST)
                    .setHttpRequest(request("request_five"))
                    .setHttpResponse(response("response_five"))
                    .setExpectation(request("request_five"), response("response_five"))
            );

            // when
            mockServerEventLog.clear(request("request_one"));

            // then
            assertThat(retrieveRequests(null), contains(
                request("request_two")
            ));
            assertThat(retrieveRequestResponseMessageLogEntries(null), contains(
                new LogEntry()
                    .setEpochTime(TimeService.currentTimeMillis())
                    .setLogLevel(INFO)
                    .setType(EXPECTATION_RESPONSE)
                    .setHttpRequest(request("request_two"))
                    .setHttpResponse(response("response_two"))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setMessageFormat("returning response:{}for request:{}for action:{}")
                    .setArguments(request("request_two"), response("response_two"), response("response_two")),
                new LogEntry()
                    .setType(FORWARDED_REQUEST)
                    .setHttpRequest(request("request_five"))
                    .setHttpResponse(response("response_five"))
                    .setExpectation(request("request_five"), response("response_five"))
            ));
            assertThat(retrieveRecordedExpectations(null), contains(
                new Expectation(request("request_five"), Times.once(), TimeToLive.unlimited(), 0).thenRespond(response("response_five"))
            ));
            List<LogEntry> actual = Objects.requireNonNull(retrieveMessageLogEntriesIncludingDeleted(null));
            assertThat(actual.get(0), is(new LogEntry()
                .setDeleted(true)
                .setLogLevel(INFO)
                .setType(RECEIVED_REQUEST)
                .setHttpRequest(request("request_one"))
                .setMessageFormat(RECEIVED_REQUEST_MESSAGE_FORMAT)
                .setArguments(request("request_one"))));
            assertThat(actual.get(1), is(new LogEntry()
                .setDeleted(true)
                .setLogLevel(INFO)
                .setType(NO_MATCH_RESPONSE)
                .setHttpRequest(request("request_one"))
                .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                .setMessageFormat("no expectation for:{}returning response:{}")
                .setArguments(request("request_one"), notFoundResponse())));
            assertThat(actual.get(2), is(new LogEntry()
                .setLogLevel(INFO)
                .setType(RECEIVED_REQUEST)
                .setHttpRequest(request("request_two"))
                .setMessageFormat(RECEIVED_REQUEST_MESSAGE_FORMAT)
                .setArguments(request("request_two"))));
            assertThat(actual.get(3), is(new LogEntry()
                .setType(EXPECTATION_MATCHED)
                .setLogLevel(INFO)
                .setHttpRequest(request("request_two"))
                .setExpectation(new Expectation(request("request_two")).thenRespond(response("response_two")))
                .setMessageFormat("request:{}matched expectation:{}")
                .setArguments(request("request_two"), new Expectation(request("request_two")).thenRespond(response("response_two")))));
            assertThat(actual.get(4), is(new LogEntry()
                .setType(EXPECTATION_RESPONSE)
                .setLogLevel(INFO)
                .setHttpRequest(request("request_two"))
                .setHttpResponse(response("response_two"))
                .setMessageFormat("request:{}matched expectation:{}")
                .setMessageFormat("returning response:{}for request:{}for action:{}")
                .setArguments(request("request_two"), response("response_two"), response("response_two"))));
            assertThat(actual.get(5), is(new LogEntry()
                .setType(TRACE)
                .setHttpRequest(request("request_four"))
                .setExpectation(new Expectation(request("request_four")).thenRespond(response("response_four")))
                .setMessageFormat("some random{}message")
                .setArguments("argument_one")));
            assertThat(actual.get(6), is(new LogEntry()
                .setType(FORWARDED_REQUEST)
                .setHttpRequest(request("request_five"))
                .setHttpResponse(response("response_five"))
                .setExpectation(request("request_five"), response("response_five"))));
            assertThat(actual.get(7), is(new LogEntry()
                .setType(CLEARED)
                .setHttpRequest(request("request_one"))
                .setMessageFormat("cleared logs that match:{}")
                .setArguments(request("request_one"))));
        } finally {
            ConfigurationProperties.logLevel(originalLevel.name());
        }
    }

    @Test
    public void shouldReset() {
        Level originalLevel = ConfigurationProperties.logLevel();
        try {
            // given
            ConfigurationProperties.logLevel("INFO");
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(NO_MATCH_RESPONSE)
                    .setHttpRequest(request("request_one"))
                    .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                    .setMessageFormat("no expectation for:{}returning response:{}")
                    .setArguments(request("request_one"), notFoundResponse())
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(INFO)
                    .setType(EXPECTATION_RESPONSE)
                    .setHttpRequest(request("request_two"))
                    .setHttpResponse(response("response_two"))
                    .setMessageFormat("returning error:{}for request:{}for action:{}")
                    .setArguments(request("request_two"), response("response_two"), response("response_two"))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_MATCHED)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_one"))
                    .setExpectation(new Expectation(request("request_one")).thenRespond(response("response_two")))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setArguments(request("request_one"), new Expectation(request("request_one")).thenRespond(response("response_two")))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(EXPECTATION_MATCHED)
                    .setLogLevel(INFO)
                    .setHttpRequest(request("request_two"))
                    .setExpectation(new Expectation(request("request_two")).thenRespond(response("response_two")))
                    .setMessageFormat("request:{}matched expectation:{}")
                    .setArguments(request("request_two"), new Expectation(request("request_two")).thenRespond(response("response_two")))
            );
            mockServerLogger.logEvent(
                new LogEntry()
                    .setType(TRACE)
                    .setHttpRequest(request("request_four"))
                    .setExpectation(new Expectation(request("request_four")).thenRespond(response("response_four")))
                    .setMessageFormat("some random{}message")
                    .setArguments("argument_one")
            );

            // when
            mockServerEventLog.reset();

            // then
            assertThat(retrieveRequests(null), empty());
            assertThat(retrieveRecordedExpectations(null), empty());
            assertThat(retrieveMessageLogEntries(null), empty());
            assertThat(retrieveRequestLogEntries(), empty());
            assertThat(retrieveRequestResponseMessageLogEntries(null), empty());
        } finally {
            ConfigurationProperties.logLevel(originalLevel.name());
        }
    }
}
