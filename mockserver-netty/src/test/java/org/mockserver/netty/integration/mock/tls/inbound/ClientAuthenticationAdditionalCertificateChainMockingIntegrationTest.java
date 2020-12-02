package org.mockserver.netty.integration.mock.tls.inbound;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.cli.Main;
import org.mockserver.client.MockServerClient;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.socket.PortFactory;
import org.mockserver.socket.tls.KeyStoreFactory;
import org.mockserver.socket.tls.jdk.JDKKeyAndCertificateFactory;
import org.mockserver.socket.tls.jdk.X509Generator;
import org.mockserver.testing.integration.mock.AbstractMockingIntegrationTestBase;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockserver.configuration.ConfigurationProperties.*;
import static org.mockserver.echo.tls.UniqueCertificateChainSSLContextBuilder.uniqueCertificateChainSSLContext;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.HttpStatusCode.OK_200;
import static org.mockserver.socket.tls.PEMToFile.privateKeyFromPEMFile;
import static org.mockserver.socket.tls.PEMToFile.x509FromPEMFile;
import static org.mockserver.stop.Stop.stopQuietly;

/**
 * @author jamesdbloom
 */
public class ClientAuthenticationAdditionalCertificateChainMockingIntegrationTest extends AbstractMockingIntegrationTestBase {

    private static final int severHttpPort = PortFactory.findFreePort();
    private static String originalCertificateAuthorityCertificate;
    private static String originalCertificateAuthorityPrivateKey;

    @BeforeClass
    public static void startServer() {
        // save original value
        originalCertificateAuthorityCertificate = certificateAuthorityCertificate();
        originalCertificateAuthorityPrivateKey = certificateAuthorityPrivateKey();

        // set new certificate authority values
        tlsMutualAuthenticationCertificateChain("org/mockserver/netty/integration/tls/ca.pem");
        tlsMutualAuthenticationRequired(true);

        Main.main("-serverPort", "" + severHttpPort);

        mockServerClient = new MockServerClient("localhost", severHttpPort).withSecure(true);
    }

    @AfterClass
    public static void stopServer() {
        stopQuietly(mockServerClient);

        // set back to original value
        certificateAuthorityCertificate(originalCertificateAuthorityCertificate);
        certificateAuthorityPrivateKey(originalCertificateAuthorityPrivateKey);
        tlsMutualAuthenticationRequired(false);
    }

    @Override
    public int getServerPort() {
        return severHttpPort;
    }

    @Test
    public void shouldReturnUpdateInHttp() {
        // when
        mockServerClient
            .when(
                request()
                    .withMethod("POST")
                    .withPath(calculatePath("some_path"))
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody("some_body_response")
            );

        // then
        assertEquals(
            response()
                .withStatusCode(426)
                .withReasonPhrase("Upgrade Required")
                .withHeader("Upgrade", "TLS/1.2, HTTP/1.1"),
            makeRequest(
                request()
                    .withPath(calculatePath("some_path"))
                    .withMethod("POST"),
                headersToIgnore)
        );
    }

    @Test
    public void shouldReturnResponseInHttpsApacheClient() throws Exception {
        // given
        mockServerClient
            .when(
                request()
                    .withMethod("POST")
                    .withPath(calculatePath("some_path"))
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody("some_body_response")
            );

        // when
        HttpClient httpClient = HttpClients.custom().setSSLContext(getSslContext()).build();
        HttpResponse response = httpClient.execute(new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost("localhost")
            .setPort(getServerPort())
            .setPath(calculatePath("some_path"))
            .build()));
        String responseBody = new String(EntityUtils.toByteArray(response.getEntity()), StandardCharsets.UTF_8);

        // then
        assertThat(response.getStatusLine().getStatusCode(), is(OK_200.code()));
        assertThat(responseBody, is("some_body_response"));
    }

    private SSLContext getSslContext() {
        JDKKeyAndCertificateFactory keyAndCertificateFactory = new JDKKeyAndCertificateFactory(new MockServerLogger());
        keyAndCertificateFactory.buildAndSavePrivateKeyAndX509Certificate();
        return new KeyStoreFactory(new MockServerLogger())
            .sslContext(
                privateKeyFromPEMFile("org/mockserver/netty/integration/tls/leaf-key-pkcs8.pem"),
                x509FromPEMFile("org/mockserver/netty/integration/tls/leaf-cert.pem"),
                x509FromPEMFile("org/mockserver/netty/integration/tls/ca.pem"),
                new X509Certificate[]{
                    x509FromPEMFile("org/mockserver/netty/integration/tls/ca.pem"),
                    keyAndCertificateFactory.certificateAuthorityX509Certificate()
                }
            );
    }

    @Test
    public void shouldFailToAuthenticateInHttpsApacheClient() {
        // given
        mockServerClient
            .when(
                request()
                    .withMethod("POST")
                    .withPath(calculatePath("some_path"))
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody("some_body_response")
            );

        // when
        try {
            HttpClient httpClient = HttpClients.custom().setSSLContext(uniqueCertificateChainSSLContext()).build();
            httpClient.execute(new HttpPost(new URIBuilder()
                .setScheme("https")
                .setHost("localhost")
                .setPort(getServerPort())
                .setPath(calculatePath("some_path"))
                .build()));

            // then
            fail("SSLHandshakeException expected");
        } catch (Throwable throwable) {
            assertThat(throwable.getMessage(),
                anyOf(
                    containsString("Received fatal alert: certificate_unknown"),
                    containsString("readHandshakeRecord"),
                    containsString("Broken pipe")
                )
            );
        }
    }

    @Test
    public void shouldFailToAuthenticateInHttpApacheClient() throws Exception {
        // given
        mockServerClient
            .when(
                request()
                    .withMethod("POST")
                    .withPath(calculatePath("some_path"))
            )
            .respond(
                response()
                    .withStatusCode(200)
                    .withBody("some_body_response")
            );

        // when
        HttpClient httpClient = HttpClients.custom().setSSLContext(getSslContext()).build();
        HttpResponse response = httpClient.execute(new HttpPost(new URIBuilder()
            .setScheme("http")
            .setHost("localhost")
            .setPort(getServerPort())
            .setPath(calculatePath("some_path"))
            .build()));
        String responseBody = new String(EntityUtils.toByteArray(response.getEntity()), StandardCharsets.UTF_8);

        // then
        assertThat(response.getStatusLine().getStatusCode(), is(426));
        assertThat(response.containsHeader("Upgrade"), is(true));
        assertThat(responseBody, is(""));
    }

}
