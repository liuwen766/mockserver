package org.mockserver.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.mockserver.lifecycle.LifeCycle;
import org.mockserver.mock.HttpState;
import org.mockserver.mock.action.http.HttpActionHandler;
import org.mockserver.netty.unification.PortUnificationHandler;
import org.mockserver.socket.tls.NettySslContextFactory;

@ChannelHandler.Sharable
public class MockServerUnificationInitializer extends ChannelHandlerAdapter {
    private final LifeCycle server;
    private final HttpState httpStateHandler;
    private final HttpActionHandler actionHandler;
    private final NettySslContextFactory nettySslContextFactory;

    public MockServerUnificationInitializer(LifeCycle server, HttpState httpStateHandler, HttpActionHandler actionHandler, NettySslContextFactory nettySslContextFactory) {
        this.server = server;
        this.httpStateHandler = httpStateHandler;
        this.actionHandler = actionHandler;
        this.nettySslContextFactory = nettySslContextFactory;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ctx.pipeline().replace(this, null, new PortUnificationHandler(server, httpStateHandler, actionHandler, nettySslContextFactory));
    }
}
