package org.mockserver.client;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.echo.http.EchoServer;
import org.mockserver.uuid.UUIDService;
import org.slf4j.event.Level;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.mockserver.character.Character.NEW_LINE;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.MediaType.TEXT_PLAIN;

/**
 * @author jamesdbloom
 */
public class MockServerClientServerValidationErrorsTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private EchoServer echoServer;
    private MockServerClient mockServerClient;

    @Before
    public void setupTestFixture() {
        echoServer = new EchoServer(false);
        mockServerClient = new MockServerClient("localhost", echoServer.getPort());
    }

    @Test
    public void shouldHandleServerValidationFailure() {
        // given
        String responseBody = "2 errors:" + NEW_LINE +
            " - object instance has properties which are not allowed by the schema: [\"paths\"] for field \"/httpRequest\"" + NEW_LINE +
            " - for field \"/httpRequest/body\" a plain string, JSON object or one of the following example bodies must be specified " + NEW_LINE +
            "   {" + NEW_LINE +
            "     \"not\": false," + NEW_LINE +
            "     \"type\": \"BINARY\"," + NEW_LINE +
            "     \"base64Bytes\": \"\"," + NEW_LINE +
            "     \"contentType\": \"\"" + NEW_LINE +
            "   }, " + NEW_LINE +
            "   {" + NEW_LINE +
            "     \"not\": false," + NEW_LINE +
            "     \"type\": \"JSON\"," + NEW_LINE +
            "     \"json\": \"\"," + NEW_LINE +
            "     \"contentType\": \"\"," + NEW_LINE +
            "     \"matchType\": \"ONLY_MATCHING_FIELDS\"" + NEW_LINE +
            "   }," + NEW_LINE +
            "   {" + NEW_LINE +
            "     \"not\": false," + NEW_LINE +
            "     \"type\": \"JSON_SCHEMA\"," + NEW_LINE +
            "     \"jsonSchema\": \"\"" + NEW_LINE +
            "   }," + NEW_LINE +
            "   {" + NEW_LINE +
            "     \"not\": false," + NEW_LINE +
            "     \"type\": \"PARAMETERS\"," + NEW_LINE +
            "     \"parameters\": {\"name\": \"value\"}" + NEW_LINE +
            "   }," + NEW_LINE +
            "   {" + NEW_LINE +
            "     \"not\": false," + NEW_LINE +
            "     \"type\": \"REGEX\"," + NEW_LINE +
            "     \"regex\": \"\"" + NEW_LINE +
            "   }," + NEW_LINE +
            "   {" + NEW_LINE +
            "     \"not\": false," + NEW_LINE +
            "     \"type\": \"STRING\"," + NEW_LINE +
            "     \"string\": \"\"" + NEW_LINE +
            "   }," + NEW_LINE +
            "   {" + NEW_LINE +
            "     \"not\": false," + NEW_LINE +
            "     \"type\": \"XML\"," + NEW_LINE +
            "     \"xml\": \"\"," + NEW_LINE +
            "     \"contentType\": \"\"" + NEW_LINE +
            "   }," + NEW_LINE +
            "   {" + NEW_LINE +
            "     \"not\": false," + NEW_LINE +
            "     \"type\": \"XML_SCHEMA\"," + NEW_LINE +
            "     \"xmlSchema\": \"\"" + NEW_LINE +
            "   }," + NEW_LINE +
            "   {" + NEW_LINE +
            "     \"not\": false," + NEW_LINE +
            "     \"type\": \"XPATH\"," + NEW_LINE +
            "     \"xpath\": \"\"" + NEW_LINE +
            "   }";
        echoServer.withNextResponse(response()
            .withStatusCode(400)
            .withContentType(TEXT_PLAIN)
            .withBody(responseBody)
        );

        // then
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString(responseBody));

        // when
        mockServerClient.when(request()).respond(response());
    }


    @Test
    public void shouldHandleOtherClientError() {
        Level originalLevel = ConfigurationProperties.logLevel();
        try {
            // given
            ConfigurationProperties.logLevel("INFO");
            UUIDService.fixedUUID = true;
            String responseBody = "some_random_response";
            echoServer.withNextResponse(response()
                .withStatusCode(401)
                .withContentType(TEXT_PLAIN)
                .withBody(responseBody)
            );

            // then
            exception.expect(ClientException.class);
            exception.expectMessage(is("error:" + NEW_LINE +
                NEW_LINE +
                "  " + responseBody + NEW_LINE +
                NEW_LINE +
                " while submitted expectation:" + NEW_LINE +
                NEW_LINE +
                "  {" + NEW_LINE +
                "    \"id\" : \"" + UUIDService.getUUID() + "\"," + NEW_LINE +
                "    \"priority\" : 0," + NEW_LINE +
                "    \"httpRequest\" : { }," + NEW_LINE +
                "    \"times\" : {" + NEW_LINE +
                "      \"unlimited\" : true" + NEW_LINE +
                "    }," + NEW_LINE +
                "    \"timeToLive\" : {" + NEW_LINE +
                "      \"unlimited\" : true" + NEW_LINE +
                "    }," + NEW_LINE +
                "    \"httpResponse\" : { }" + NEW_LINE +
                "  }" + NEW_LINE
            ));

            // when
            mockServerClient.when(request()).respond(response());
        } finally {
            UUIDService.fixedUUID = false;
            ConfigurationProperties.logLevel(originalLevel.name());
        }
    }
}
