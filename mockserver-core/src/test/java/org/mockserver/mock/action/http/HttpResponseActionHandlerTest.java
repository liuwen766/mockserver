package org.mockserver.mock.action.http;

import org.junit.Test;
import org.mockserver.mock.action.http.HttpResponseActionHandler;
import org.mockserver.model.HttpResponse;

import static org.mockito.Mockito.*;

/**
 * @author jamesdbloom
 */
public class HttpResponseActionHandlerTest {

    @Test
    public void shouldHandleHttpRequests() {
        // given
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpResponseActionHandler httpResponseActionHandler = new HttpResponseActionHandler();

        // when
        httpResponseActionHandler.handle(httpResponse);

        // then
        verify(httpResponse).clone();
    }

}
