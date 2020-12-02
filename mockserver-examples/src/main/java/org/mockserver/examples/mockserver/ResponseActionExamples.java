package org.mockserver.examples.mockserver;

import com.google.common.io.ByteStreams;
import org.mockserver.client.MockServerClient;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.model.HttpTemplate;
import org.mockserver.model.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_DISPOSITION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.mockserver.character.Character.NEW_LINE;
import static org.mockserver.model.BinaryBody.binary;
import static org.mockserver.model.ConnectionOptions.connectionOptions;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.HttpTemplate.template;
import static org.mockserver.model.JsonBody.json;

/**
 * @author jamesdbloom
 */
public class ResponseActionExamples {

    public void responseLiteralWithBodyOnly() {
        new MockServerClient("localhost", 1080)
            // this request matcher matches every request
            .when(
                request()
            )
            .respond(
                response()
                    .withBody("some_response_body")
            );
    }

    public void responseLiteralWithUTF16BodyResponse() {
        new MockServerClient("localhost", 1080)
            // this request matcher matches every request
            .when(
                request()
            )
            .respond(
                response()
                    .withHeader(
                        CONTENT_TYPE.toString(),
                        MediaType.create("text", "plain").withCharset(StandardCharsets.UTF_16).toString()
                    )
                    .withBody("我说中国话".getBytes(StandardCharsets.UTF_16))
            );
    }

    public void jsonResponseWithUTF8Body() {
        new MockServerClient("localhost", 1081)
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/farsi_body")
            )
            .respond(
                response()
                    .withBody(json("", MediaType.APPLICATION_JSON_UTF_8))
            );
    }

    public void responseLiteralWithHeader() {
        new MockServerClient("localhost", 1080)
            // this request matcher matches every request
            .when(
                request()
            )
            .respond(
                response()
                    .withBody("some_response_body")
                    .withHeader("Content-Type", "plain/text")
            );
    }

    public void responseLiteralWithCookie() {
        new MockServerClient("localhost", 8080)
            // this request matcher matches every request
            //localhost:1080
            .when(
                request()
            )
            .respond(
                response()
                    .withHeaders(
                        new Header(CONTENT_TYPE.toString(), org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
                    )
                    .withBody("" +
                        "[" + NEW_LINE +
                        "    {" + NEW_LINE +
                        "        \"id\": \"1\"," + NEW_LINE +
                        "        \"title\": \"123456\"," + NEW_LINE +
                        "        \"author\": \"111\"," + NEW_LINE +
                        "        \"isbn\": \"0691067570\"," + NEW_LINE +
                        "        \"publicationDate\": \"1989\"" + NEW_LINE +
                        "    }," + NEW_LINE +
                        "    {" + NEW_LINE +
                        "        \"id\": \"2\"," + NEW_LINE +
                        "        \"title\": \"354321\"," + NEW_LINE +
                        "        \"author\": \"222\"," + NEW_LINE +
                        "        \"isbn\": \"1568984308\"," + NEW_LINE +
                        "        \"publicationDate\": \"2004\"" + NEW_LINE +
                        "    }," + NEW_LINE +
                        "    {" + NEW_LINE +
                        "        \"id\": \"3\"," + NEW_LINE +
                        "        \"title\": \"123321\"," + NEW_LINE +
                        "        \"author\": \"333\"," + NEW_LINE +
                        "        \"isbn\": \"0345372050\"," + NEW_LINE +
                        "        \"publicationDate\": \"1990\"" + NEW_LINE +
                        "    }" +
                        "]")
//                response()
//                    .withBody("some_response_body")
//                    .withHeader("Content-Type", "plain/text")
//                    .withCookie("Session", "97d43b1e-fe03-4855-926a-f448eddac32f")
            );
    }

    public void responseLiteralWithStatusCodeAndReasonPhraseOnly() {
        new MockServerClient("localhost", 1080)
            .when(
                request()
                    .withMethod("POST")
                    .withPath("/some/path")
            )
            .respond(
                response()
                    .withStatusCode(418)
                    .withReasonPhrase("I'm a teapot")
            );
    }

    public void respondDifferentlyForTheSameRequest() {
        MockServerClient mockServerClient = new MockServerClient("localhost", 1082);

// respond once with 200, then respond twice with 204, then
// respond with 404 as no remaining active expectations
        mockServerClient
            .when(
                request()
                    .withPath("/some/path"),
                Times.exactly(1)
            )
            .respond(
                response()
                    .withStatusCode(200)
            );

        mockServerClient
            .when(
                request()
                    .withPath("/some/path"),
                Times.exactly(2)
            )
            .respond(
                response()
                    .withStatusCode(204)
            );
    }

    @SuppressWarnings("ConstantConditions")
    public void responseLiteralWithBinaryPNGBody() throws IOException {
        byte[] pngBytes = ByteStreams.toByteArray(getClass().getClassLoader().getResourceAsStream("org/mockserver/examples/mockserver/test.png"));
        new MockServerClient("localhost", 1080)
            .when(
                request()
                    .withPath("/ws/rest/user/[0-9]+/icon/[0-9]+\\.png")
            )
            .respond(
                response()
                    .withStatusCode(HttpStatusCode.OK_200.code())
                    .withHeaders(
                        header(CONTENT_TYPE.toString(), MediaType.PNG.toString()),
                        header(CONTENT_DISPOSITION.toString(), "form-data; name=\"test.png\"; filename=\"test.png\"")
                    )
                    .withBody(binary(pngBytes))
            );
    }

    public void responseLiteralWith10SecondDelay() {
        new MockServerClient("localhost", 1080)
            .when(
                request()
                    .withPath("/bcdig/v1/user/userinfo")
            )
            .respond(
                response()
                    .withBody("some_response_body")
                    .withDelay(TimeUnit.SECONDS, 10)
            );
    }

    public void responseLiteralWithConnectionOptionsToSuppressHeaders() {
        new MockServerClient("localhost", 1080)
            .when(
                request()
                    .withPath("/some/path")
            )
            .respond(
                response()
                    .withBody("some_response_body")
                    .withConnectionOptions(
                        connectionOptions()
                            .withSuppressConnectionHeader(true)
                            .withSuppressContentLengthHeader(true)
                    )
            );
    }

    public void responseLiteralWithConnectionOptionsToOverrideHeaders() {
        new MockServerClient("localhost", 1080)
            .when(
                request()
                    .withPath("/some/path")
            )
            .respond(
                response()
                    .withBody("some_response_body")
                    .withConnectionOptions(
                        connectionOptions()
                            .withKeepAliveOverride(false)
                            .withContentLengthHeaderOverride(10)
                    )
            );
    }

    public void responseLiteralWithConnectionOptionsToCloseSocket() {
        new MockServerClient("localhost", 1080)
            .when(
                request()
                    .withPath("/some/path")
            )
            .respond(
                response()
                    .withBody("some_response_body")
                    .withConnectionOptions(
                        connectionOptions()
                            .withCloseSocket(true)
                    )
            );
    }

    public void javascriptTemplatedResponse() {
        new MockServerClient("localhost", 1080)
            .when(
                request()
                    .withPath("/some/path")
            )
            .respond(
                template(
                    HttpTemplate.TemplateType.JAVASCRIPT,
                    "return {" + System.getProperty("line.separator") +
                        "     'statusCode': 200," + System.getProperty("line.separator") +
                        "     'cookies': {" + System.getProperty("line.separator") +
                        "          'session' : request.headers['session-id'][0]" + System.getProperty("line.separator") +
                        "     }," + System.getProperty("line.separator") +
                        "     'headers': {" + System.getProperty("line.separator") +
                        "          'Date' : Date()" + System.getProperty("line.separator") +
                        "     }," + System.getProperty("line.separator") +
                        "     'body': JSON.stringify(" + System.getProperty("line.separator") +
                        "               {" + System.getProperty("line.separator") +
                        "                    method: request.method," + System.getProperty("line.separator") +
                        "                    path: request.path," + System.getProperty("line.separator") +
                        "                    body: request.body" + System.getProperty("line.separator") +
                        "               }" + System.getProperty("line.separator") +
                        "          )" + System.getProperty("line.separator") +
                        "};"
                )
            );
    }

    public void javascriptTemplatedResponseWithDelay() {
        String template = "" +
            "if (request.method === 'POST' && request.path === '/somePath') {" + System.getProperty("line.separator") +
            "    return {" + System.getProperty("line.separator") +
            "        'statusCode': 200," + System.getProperty("line.separator") +
            "        'body': JSON.stringify({name: 'value'})" + System.getProperty("line.separator") +
            "    };" + System.getProperty("line.separator") +
            "} else {" + System.getProperty("line.separator") +
            "    return {" + System.getProperty("line.separator") +
            "        'statusCode': 406," + System.getProperty("line.separator") +
            "        'body': request.body" + System.getProperty("line.separator") +
            "    };" + System.getProperty("line.separator") +
            "}";

        new MockServerClient("localhost", 1080)
            .when(
                request()
                    .withPath("/some/path")
            )
            .respond(
                template(HttpTemplate.TemplateType.JAVASCRIPT)
                    .withTemplate(template)
                    .withDelay(TimeUnit.MINUTES, 2)
            );

    }

    public void velocityTemplatedResponse() {
        new MockServerClient("localhost", 1080)
            .when(
                request()
                    .withPath("/some/path")
            )
            .respond(
                template(
                    HttpTemplate.TemplateType.VELOCITY,
                    "{" + System.getProperty("line.separator") +
                        "     \"statusCode\": 200," + System.getProperty("line.separator") +
                        "     \"cookies\": { " + System.getProperty("line.separator") +
                        "          \"session\": \"$!request.headers['Session-Id'][0]\"" + System.getProperty("line.separator") +
                        "     }," + System.getProperty("line.separator") +
                        "     \"headers\": {" + System.getProperty("line.separator") +
                        "          \"Client-User-Agent\": [ \"$!request.headers['User-Agent'][0]\" ]" + System.getProperty("line.separator") +
                        "     }," + System.getProperty("line.separator") +
                        "     \"body\": $!request.body" + System.getProperty("line.separator") +
                        "}"
                )
            );
    }

    public static void main(String[] args) throws InterruptedException {
        ConfigurationProperties.logLevel("DEBUG");
        new ClientAndServer(1080);
        new ResponseActionExamples().responseLiteralWithCookie();

//        new ClientAndServer(1081);
//        // localhost:1081/farsi_body
//        new ResponseActionExamples().jsonResponseWithUTF8Body();
//
//        new ClientAndServer(1082);
//        //  localhost:1082/some/path
//        new ResponseActionExamples().respondDifferentlyForTheSameRequest();
//
//        new ClientAndServer(1080);
//        // localhost:1083
//        new ResponseActionExamples().responseLiteralWithUTF16BodyResponse();
//
//        new ClientAndServer(1080);
//        // localhost:1084/bcdig/v1/user/userinfo
//        new ResponseActionExamples().responseLiteralWith10SecondDelay();

        MINUTES.sleep(5);
    }
}
