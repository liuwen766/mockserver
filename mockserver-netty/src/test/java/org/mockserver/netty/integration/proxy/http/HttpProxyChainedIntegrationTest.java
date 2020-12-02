package org.mockserver.netty.integration.proxy.http;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.NettyHttpClient;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.model.HttpResponse;
import org.mockserver.scheduler.Scheduler;
import org.mockserver.uuid.UUIDService;

import java.net.InetSocketAddress;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.stop.Stop.stopQuietly;
import static org.mockserver.verify.VerificationTimes.exactly;

/**
 * @author jamesdbloom
 */
public class HttpProxyChainedIntegrationTest {

    private static ClientAndServer targetClientAndServer;

    private static final EventLoopGroup clientEventLoopGroup = new NioEventLoopGroup(3, new Scheduler.SchedulerThreadFactory(HttpProxyChainedIntegrationTest.class.getSimpleName() + "-eventLoop"));

    @BeforeClass
    public static void startServer() {
        targetClientAndServer = startClientAndServer();
    }

    @AfterClass
    public static void stopEventLoopGroup() {
        clientEventLoopGroup.shutdownGracefully(0, 0, MILLISECONDS).syncUninterruptibly();
    }

    @AfterClass
    public static void stopServer() {
        stopQuietly(targetClientAndServer);

    }

    @Before
    public void reset() {
        targetClientAndServer.reset();
    }

    @Test
    public void shouldAuthenticateForwardHTTPConnect() throws Exception {
        String existingUsername = ConfigurationProperties.proxyAuthenticationUsername();
        String existingPassword = ConfigurationProperties.proxyAuthenticationPassword();
        String existingForwardUsername = ConfigurationProperties.forwardProxyAuthenticationUsername();
        String existingForwardPassword = ConfigurationProperties.forwardProxyAuthenticationPassword();

        String forwardHttpsProxy = System.getProperty("mockserver.forwardHttpsProxy");
        ClientAndServer proxyClientAndServer = null;
        try {

            String username = UUIDService.getUUID();
            String password = UUIDService.getUUID();
            ConfigurationProperties.proxyAuthenticationUsername(username);
            ConfigurationProperties.proxyAuthenticationPassword(password);
            ConfigurationProperties.forwardHttpsProxy("localhost:" + targetClientAndServer.getPort());
            ConfigurationProperties.forwardProxyAuthenticationUsername(username);
            ConfigurationProperties.forwardProxyAuthenticationPassword(password);
            proxyClientAndServer = startClientAndServer();

            HttpResponse httpResponse = new NettyHttpClient(new MockServerLogger(), clientEventLoopGroup, null, false)
                .sendRequest(
                    request()
                        .withPath("/target")
                        .withSecure(true)
                        .withHeader(HOST.toString(), "www.mock-server.com"),
                    new InetSocketAddress(proxyClientAndServer.getLocalPort())
                )
                .get(10, SECONDS);

            // and - both proxy and target verify request received
            proxyClientAndServer.verify(request().withPath("/target"));
            targetClientAndServer.verify(request().withPath("/target"));

        } finally {
            if (proxyClientAndServer != null) {
                stopQuietly(proxyClientAndServer);
            }

            ConfigurationProperties.proxyAuthenticationUsername(existingUsername);
            ConfigurationProperties.proxyAuthenticationPassword(existingPassword);
            ConfigurationProperties.forwardProxyAuthenticationUsername(existingForwardUsername);
            ConfigurationProperties.forwardProxyAuthenticationPassword(existingForwardPassword);
            if (forwardHttpsProxy != null) {
                System.setProperty("mockserver.forwardHttpsProxy", forwardHttpsProxy);
            } else {
                System.clearProperty("mockserver.forwardHttpsProxy");
            }
        }
    }

    @Test
    public void shouldForwardHTTPConnect() throws Exception {
        String forwardHttpsProxy = System.getProperty("mockserver.forwardHttpsProxy");
        ClientAndServer proxyClientAndServer = null;
        try {

            ConfigurationProperties.forwardHttpsProxy("localhost:" + targetClientAndServer.getLocalPort());
            proxyClientAndServer = startClientAndServer();

            HttpResponse httpResponse = new NettyHttpClient(new MockServerLogger(), clientEventLoopGroup, null, false)
                .sendRequest(
                    request()
                        .withPath("/target")
                        .withSecure(true)
                        .withHeader(HOST.toString(), "www.mock-server.com"),
                    new InetSocketAddress(proxyClientAndServer.getLocalPort())
                )
                .get(10, SECONDS);

            // and - both proxy and target verify request received
            proxyClientAndServer.verify(request().withPath("/target"));
            targetClientAndServer.verify(request().withPath("/target"));

        } finally {
            if (proxyClientAndServer != null) {
                stopQuietly(proxyClientAndServer);
            }

            if (forwardHttpsProxy != null) {
                System.setProperty("mockserver.forwardHttpsProxy", forwardHttpsProxy);
            } else {
                System.clearProperty("mockserver.forwardHttpsProxy");
            }
        }
    }

    @Test
    public void shouldNotForwardHTTPConnectIfNotSecure() throws Exception {
        String forwardHttpsProxy = System.getProperty("mockserver.forwardHttpsProxy");
        ClientAndServer proxyClientAndServer = null;
        try {

            ConfigurationProperties.forwardHttpsProxy("localhost:" + targetClientAndServer.getLocalPort());
            proxyClientAndServer = startClientAndServer();

            HttpResponse httpResponse = new NettyHttpClient(new MockServerLogger(), clientEventLoopGroup, null, false)
                .sendRequest(
                    request()
                        .withPath("/target")
                        .withHeader(HOST.toString(), "www.mock-server.com"),
                    new InetSocketAddress(proxyClientAndServer.getLocalPort())
                )
                .get(10, SECONDS);

            // and - both proxy and target verify request received
            proxyClientAndServer.verify(request().withPath("/target"));
            targetClientAndServer.verify(request().withPath("/target"), exactly(0));

        } finally {
            if (proxyClientAndServer != null) {
                stopQuietly(proxyClientAndServer);
            }

            if (forwardHttpsProxy != null) {
                System.setProperty("mockserver.forwardHttpsProxy", forwardHttpsProxy);
            } else {
                System.clearProperty("mockserver.forwardHttpsProxy");
            }
        }
    }

}
