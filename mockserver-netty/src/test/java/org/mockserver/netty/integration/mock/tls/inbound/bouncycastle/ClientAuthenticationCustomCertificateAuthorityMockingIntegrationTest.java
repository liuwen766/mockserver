package org.mockserver.netty.integration.mock.tls.inbound.bouncycastle;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockserver.cli.Main;
import org.mockserver.client.MockServerClient;
import org.mockserver.netty.integration.mock.tls.inbound.AbstractClientAuthenticationMockingIntegrationTest;
import org.mockserver.socket.PortFactory;

import static org.mockserver.configuration.ConfigurationProperties.*;
import static org.mockserver.stop.Stop.stopQuietly;

/**
 * @author jamesdbloom
 */
public class ClientAuthenticationCustomCertificateAuthorityMockingIntegrationTest extends AbstractClientAuthenticationMockingIntegrationTest {

    private static final int severHttpPort = PortFactory.findFreePort();
    private static String originalCertificateAuthorityCertificate;
    private static String originalCertificateAuthorityPrivateKey;

    @BeforeClass
    public static void startServer() {
        // save original value
        originalCertificateAuthorityCertificate = certificateAuthorityCertificate();
        originalCertificateAuthorityPrivateKey = certificateAuthorityPrivateKey();

        // set new certificate authority values
        certificateAuthorityCertificate("org/mockserver/netty/integration/tls/ca.pem");
        certificateAuthorityPrivateKey("org/mockserver/netty/integration/tls/ca-key-pkcs8.pem");
        useBouncyCastleForKeyAndCertificateGeneration(true);
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
        useBouncyCastleForKeyAndCertificateGeneration(false);
        tlsMutualAuthenticationRequired(false);
    }

    @Override
    public int getServerPort() {
        return severHttpPort;
    }

}
