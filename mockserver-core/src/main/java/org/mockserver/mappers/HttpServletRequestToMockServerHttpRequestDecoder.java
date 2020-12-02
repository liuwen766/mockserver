package org.mockserver.mappers;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.mockserver.codec.BodyServletDecoderEncoder;
import org.mockserver.codec.ExpandedParameterDecoder;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.model.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author jamesdbloom
 */
public class HttpServletRequestToMockServerHttpRequestDecoder {

    private final BodyServletDecoderEncoder bodyDecoderEncoder;
    private final ExpandedParameterDecoder formParameterParser;

    public HttpServletRequestToMockServerHttpRequestDecoder(MockServerLogger mockServerLogger) {
        bodyDecoderEncoder = new BodyServletDecoderEncoder(mockServerLogger);
        formParameterParser = new ExpandedParameterDecoder(mockServerLogger);
    }

    public HttpRequest mapHttpServletRequestToMockServerRequest(HttpServletRequest httpServletRequest) {
        HttpRequest request = new HttpRequest();
        setMethod(request, httpServletRequest);

        setPath(request, httpServletRequest);
        setQueryString(request, httpServletRequest);

        setBody(request, httpServletRequest);
        setHeaders(request, httpServletRequest);
        setCookies(request, httpServletRequest);
        setSocketAddress(request, httpServletRequest);

        request.withKeepAlive(isKeepAlive(httpServletRequest));
        request.withSecure(httpServletRequest.isSecure());
        return request;
    }

    private void setMethod(HttpRequest httpRequest, HttpServletRequest httpServletRequest) {
        httpRequest.withMethod(httpServletRequest.getMethod());
    }

    private void setPath(HttpRequest httpRequest, HttpServletRequest httpServletRequest) {
        httpRequest.withPath(httpServletRequest.getPathInfo() != null && httpServletRequest.getContextPath() != null ? httpServletRequest.getPathInfo() : httpServletRequest.getRequestURI());
    }

    private void setQueryString(HttpRequest httpRequest, HttpServletRequest httpServletRequest) {
        Parameters parameters;
        if (isNotBlank(httpServletRequest.getQueryString())) {
            parameters = formParameterParser.retrieveFormParameters(httpServletRequest.getQueryString(), false);
        } else {
            parameters = new Parameters();
        }
        httpRequest.withQueryStringParameters(parameters);
    }

    private void setBody(HttpRequest httpRequest, HttpServletRequest httpServletRequest) {
        httpRequest.withBody(bodyDecoderEncoder.servletRequestToBody(httpServletRequest));
    }

    private void setHeaders(HttpRequest httpRequest, HttpServletRequest httpServletRequest) {
        Headers headers = new Headers();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            List<String> mappedHeaderValues = new ArrayList<>();
            Enumeration<String> headerValues = httpServletRequest.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                mappedHeaderValues.add(headerValues.nextElement());
            }
            headers.withEntry(headerName, mappedHeaderValues);
        }
        httpRequest.withHeaders(headers);
    }

    private void setCookies(HttpRequest httpRequest, HttpServletRequest httpServletRequest) {
        Cookies cookies = new Cookies();
        if (httpServletRequest.getCookies() != null) {
            for (javax.servlet.http.Cookie cookie : httpServletRequest.getCookies()) {
                cookies.withEntry(new Cookie(cookie.getName(), cookie.getValue()));
            }
        }
        httpRequest.withCookies(cookies);
    }

    private void setSocketAddress(HttpRequest httpRequest, HttpServletRequest httpServletRequest) {
        httpRequest.withSocketAddress(httpServletRequest.isSecure(), httpServletRequest.getHeader("host"), httpServletRequest.getLocalPort());
    }

    public boolean isKeepAlive(HttpServletRequest httpServletRequest) {
        CharSequence connection = httpServletRequest.getHeader(HttpHeaderNames.CONNECTION.toString());
        if (HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(connection)) {
            return false;
        }

        if (httpServletRequest.getProtocol().equals("HTTP/1.1")) {
            return !HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(connection);
        } else {
            return HttpHeaderValues.KEEP_ALIVE.contentEqualsIgnoreCase(connection);
        }
    }
}
