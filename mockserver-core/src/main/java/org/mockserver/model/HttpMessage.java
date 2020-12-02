package org.mockserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author jamesdbloom
 */
@SuppressWarnings("rawtypes")
public interface HttpMessage<T extends HttpMessage, B extends Body> extends Message {

    T withBody(String body);

    T withBody(String body, Charset charset);

    T withBody(byte[] body);

    T withBody(B body);

    B getBody();

    @JsonIgnore
    byte[] getBodyAsRawBytes();

    @JsonIgnore
    String getBodyAsString();

    Headers getHeaders();

    T withHeaders(Headers headers);

    T withHeaders(List<Header> headers);

    T withHeaders(Header... headers);

    T withHeader(Header header);

    T withHeader(String name, String... values);

    T withHeader(NottableString name, NottableString... values);

    T withContentType(MediaType mediaType);

    T replaceHeader(Header header);

    List<Header> getHeaderList();

    List<String> getHeader(String name);

    String getFirstHeader(String name);

    boolean containsHeader(String name);

    T removeHeader(String name);

    T removeHeader(NottableString name);

    Cookies getCookies();

    T withCookies(Cookies cookies);

    T withCookies(List<Cookie> cookies);

    T withCookies(Cookie... cookies);

    T withCookie(Cookie cookie);

    T withCookie(String name, String value);

    T withCookie(NottableString name, NottableString value);

    List<Cookie> getCookieList();
}
