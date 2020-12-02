package org.mockserver.mock.action.http;

import org.junit.Test;
import org.mockserver.closurecallback.websocketregistry.WebSocketClientRegistry;
import org.mockserver.closurecallback.websocketregistry.WebSocketResponseCallback;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.mock.HttpState;
import org.mockserver.model.HttpObjectCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.responsewriter.ResponseWriter;

import static org.mockito.Mockito.*;
import static org.mockserver.closurecallback.websocketregistry.WebSocketClientRegistry.WEB_SOCKET_CORRELATION_ID_HEADER_NAME;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.notFoundResponse;

/**
 * @author jamesdbloom
 */
public class HttpResponseObjectCallbackActionHandlerTest {

    @Test
    public void shouldHandleHttpRequests() {
        // given
        WebSocketClientRegistry mockWebSocketClientRegistry = mock(WebSocketClientRegistry.class);
        HttpState mockHttpStateHandler = mock(HttpState.class);
        HttpObjectCallback httpObjectCallback = new HttpObjectCallback().withClientId("some_clientId");
        HttpRequest request = request().withBody("some_body");
        ResponseWriter mockResponseWriter = mock(ResponseWriter.class);
        when(mockHttpStateHandler.getWebSocketClientRegistry()).thenReturn(mockWebSocketClientRegistry);
        when(mockHttpStateHandler.getMockServerLogger()).thenReturn(new MockServerLogger());

        // when
        new HttpResponseObjectCallbackActionHandler(mockHttpStateHandler).handle(mock(HttpActionHandler.class), httpObjectCallback, request, mockResponseWriter, true, null);

        // then
        verify(mockWebSocketClientRegistry).registerResponseCallbackHandler(any(String.class), any(WebSocketResponseCallback.class));
        verify(mockWebSocketClientRegistry).sendClientMessage(eq("some_clientId"), any(HttpRequest.class), isNull());
    }

    @Test
    public void shouldReturnNotFound() {
        // given
        HttpActionHandler mockActionHandler = mock(HttpActionHandler.class);
        HttpState mockHttpStateHandler = mock(HttpState.class);
        WebSocketClientRegistry mockWebSocketClientRegistry = mock(WebSocketClientRegistry.class);
        HttpObjectCallback httpObjectCallback = new HttpObjectCallback().withClientId("some_clientId");
        HttpRequest request = request().withBody("some_body");
        ResponseWriter mockResponseWriter = mock(ResponseWriter.class);
        when(mockHttpStateHandler.getWebSocketClientRegistry()).thenReturn(mockWebSocketClientRegistry);
        when(mockHttpStateHandler.getMockServerLogger()).thenReturn(new MockServerLogger());
        when(mockWebSocketClientRegistry.sendClientMessage(eq("some_clientId"), any(HttpRequest.class), isNull())).thenReturn(false);

        // when
        new HttpResponseObjectCallbackActionHandler(mockHttpStateHandler).handle(mockActionHandler, httpObjectCallback, request, mockResponseWriter, true, null);

        // then
        verify(mockWebSocketClientRegistry).registerResponseCallbackHandler(any(String.class), any(WebSocketResponseCallback.class));
        verify(mockWebSocketClientRegistry).sendClientMessage(eq("some_clientId"), any(HttpRequest.class), isNull());
        verify(mockActionHandler).writeResponseActionResponse(notFoundResponse().removeHeader(WEB_SOCKET_CORRELATION_ID_HEADER_NAME), mockResponseWriter, request, httpObjectCallback, true);
    }
}
