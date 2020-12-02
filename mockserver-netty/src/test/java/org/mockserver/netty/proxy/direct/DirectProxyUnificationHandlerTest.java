package org.mockserver.netty.proxy.direct;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.socks.SocksInitRequestDecoder;
import io.netty.handler.codec.socks.SocksMessageEncoder;
import io.netty.handler.ssl.SslHandler;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import org.mockserver.lifecycle.LifeCycle;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.mock.HttpState;
import org.mockserver.mock.action.http.HttpActionHandler;
import org.mockserver.netty.MockServerUnificationInitializer;
import org.mockserver.netty.proxy.socks.Socks5ProxyHandler;
import org.mockserver.scheduler.Scheduler;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.slf4j.event.Level.TRACE;

public class DirectProxyUnificationHandlerTest {

    @Test
    public void shouldSwitchToSsl() {
        // given
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new MockServerUnificationInitializer(mock(LifeCycle.class), new HttpState(new MockServerLogger(), mock(Scheduler.class)), mock(HttpActionHandler.class), null));

        // and - no SSL handler
        assertThat(embeddedChannel.pipeline().get(SslHandler.class), is(nullValue()));

        // when - first part of a 5-byte handshake message
        embeddedChannel.writeInbound(Unpooled.wrappedBuffer(new byte[]{
            22, // handshake
            3,  // major version
            1,
            0,
            5   // package length (5-byte)
        }));

        // then - should add SSL handlers first
        if (MockServerLogger.isEnabled(TRACE)) {
            assertThat(String.valueOf(embeddedChannel.pipeline().names()), embeddedChannel.pipeline().names(), contains(
                "SniHandler#0",
                "LoggingHandler#0",
                "PortUnificationHandler#0",
                "DefaultChannelPipeline$TailContext#0"
            ));
        } else {
            assertThat(String.valueOf(embeddedChannel.pipeline().names()), embeddedChannel.pipeline().names(), contains(
                "SniHandler#0",
                "PortUnificationHandler#0",
                "DefaultChannelPipeline$TailContext#0"
            ));
        }
    }

    @Test
    public void shouldSwitchToSOCKS() {
        // given - embedded channel
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new MockServerUnificationInitializer(mock(LifeCycle.class), new HttpState(new MockServerLogger(), mock(Scheduler.class)), mock(HttpActionHandler.class), null));

        // and - no SOCKS handlers
        assertThat(embeddedChannel.pipeline().get(Socks5ProxyHandler.class), is(nullValue()));
        assertThat(embeddedChannel.pipeline().get(SocksMessageEncoder.class), is(nullValue()));
        assertThat(embeddedChannel.pipeline().get(SocksInitRequestDecoder.class), is(nullValue()));

        // when - SOCKS INIT message
        embeddedChannel.writeInbound(Unpooled.wrappedBuffer(new byte[]{
            (byte) 0x05,                                        // SOCKS5
            (byte) 0x02,                                        // 1 authentication method
            (byte) 0x00,                                        // NO_AUTH
            (byte) 0x02,                                        // AUTH_PASSWORD
        }));


        // then - INIT response
        assertThat(ByteBufUtil.hexDump((ByteBuf) embeddedChannel.readOutbound()), is(Hex.encodeHexString(new byte[]{
            (byte) 0x05,                                        // SOCKS5
            (byte) 0x00,                                        // NO_AUTH
        })));

        // and then - should add SOCKS handlers first
        if (MockServerLogger.isEnabled(TRACE)) {
            assertThat(String.valueOf(embeddedChannel.pipeline().names()), embeddedChannel.pipeline().names(), contains(
                "LoggingHandler#0",
                "Socks5CommandRequestDecoder#0",
                "Socks5ServerEncoder#0",
                "Socks5ProxyHandler#0",
                "PortUnificationHandler#0",
                "DefaultChannelPipeline$TailContext#0"
            ));
        } else {
            assertThat(String.valueOf(embeddedChannel.pipeline().names()), embeddedChannel.pipeline().names(), contains(
                "Socks5CommandRequestDecoder#0",
                "Socks5ServerEncoder#0",
                "Socks5ProxyHandler#0",
                "PortUnificationHandler#0",
                "DefaultChannelPipeline$TailContext#0"
            ));
        }
    }

    @Test
    public void shouldSwitchToHttp() {
        // given
        EmbeddedChannel embeddedChannel = new EmbeddedChannel();
        embeddedChannel.pipeline().addLast(new MockServerUnificationInitializer(mock(LifeCycle.class), new HttpState(new MockServerLogger(), mock(Scheduler.class)), mock(HttpActionHandler.class), null));

        // and - no HTTP handlers
        assertThat(embeddedChannel.pipeline().get(HttpServerCodec.class), is(nullValue()));
        assertThat(embeddedChannel.pipeline().get(HttpContentDecompressor.class), is(nullValue()));
        assertThat(embeddedChannel.pipeline().get(HttpObjectAggregator.class), is(nullValue()));

        // when - basic HTTP request
        embeddedChannel.writeInbound(Unpooled.wrappedBuffer("GET /somePath HTTP/1.1\r\nHost: some.random.host\r\n\r\n".getBytes(UTF_8)));

        // then - should add HTTP handlers last
        if (MockServerLogger.isEnabled(TRACE)) {
            assertThat(String.valueOf(embeddedChannel.pipeline().names()), embeddedChannel.pipeline().names(), contains(
                "LoggingHandler#0",
                "HttpServerCodec#0",
                "HttpContentDecompressor#0",
                "HttpContentLengthRemover#0",
                "HttpObjectAggregator#0",
                "CallbackWebSocketServerHandler#0",
                "DashboardWebSocketHandler#0",
                "MockServerHttpServerCodec#0",
                "HttpRequestHandler#0",
                "DefaultChannelPipeline$TailContext#0"
            ));
        } else {
            assertThat(String.valueOf(embeddedChannel.pipeline().names()), embeddedChannel.pipeline().names(), contains(
                "HttpServerCodec#0",
                "HttpContentDecompressor#0",
                "HttpContentLengthRemover#0",
                "HttpObjectAggregator#0",
                "CallbackWebSocketServerHandler#0",
                "DashboardWebSocketHandler#0",
                "MockServerHttpServerCodec#0",
                "HttpRequestHandler#0",
                "DefaultChannelPipeline$TailContext#0"
            ));
        }
    }

    @Test
    public void shouldSupportUnknownProtocol() {
        // given
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new MockServerUnificationInitializer(mock(LifeCycle.class), new HttpState(new MockServerLogger(), mock(Scheduler.class)), mock(HttpActionHandler.class), null));

        // and - channel open
        assertThat(embeddedChannel.isOpen(), is(true));

        // when - basic HTTP request
        embeddedChannel.writeInbound(Unpooled.wrappedBuffer("UNKNOWN_PROTOCOL".getBytes(UTF_8)));

        // then - should add no handlers
        assertThat(embeddedChannel.pipeline().names(), contains(
            "DefaultChannelPipeline$TailContext#0"
        ));

        // and - close channel
        assertThat(embeddedChannel.isOpen(), is(false));
    }
}
