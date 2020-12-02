package org.mockserver.netty.proxy.socks;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.socksx.v5.*;
import org.mockserver.lifecycle.LifeCycle;
import org.mockserver.logging.MockServerLogger;

@ChannelHandler.Sharable
public final class Socks5ConnectHandler extends SocksConnectHandler<Socks5CommandRequest> {

    public Socks5ConnectHandler(LifeCycle server, MockServerLogger mockServerLogger, String host, int port) {
        super(server, mockServerLogger, host, port);
    }

    @Override
    protected void removeCodecSupport(ChannelHandlerContext ctx) {
        super.removeCodecSupport(ctx);
        removeHandler(ctx.pipeline(), Socks5ServerEncoder.class);
    }

    @Override
    protected Object successResponse(Object request) {
        return new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.DOMAIN, host, port);
    }

    @Override
    protected Object failureResponse(Object request) {
        return new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.DOMAIN, host, port);
    }
}
