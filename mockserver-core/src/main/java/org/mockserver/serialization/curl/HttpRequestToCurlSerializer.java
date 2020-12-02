package org.mockserver.serialization.curl;

import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.mappers.MockServerHttpRequestToFullHttpRequest;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.NottableString;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;
import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author jamesdbloom
 */
public class HttpRequestToCurlSerializer {

    private final MockServerLogger mockServerLogger;

    public HttpRequestToCurlSerializer(MockServerLogger mockServerLogger) {
        this.mockServerLogger = mockServerLogger;
    }

    public String toCurl(HttpRequest request) {
        return toCurl(request, null);
    }

    public String toCurl(HttpRequest request, @Nullable InetSocketAddress remoteAddress) {
        StringBuilder curlString = new StringBuilder();
        if (request != null) {
            if (isNotBlank(request.getFirstHeader(HOST.toString())) || remoteAddress != null) {
                boolean isSsl = request.isSecure() != null && request.isSecure();
                curlString.append("curl -v");
                curlString.append(" ");
                curlString.append("'");
                curlString.append((isSsl ? "https" : "http"));
                curlString.append("://");
                curlString.append(getHostAndPort(request, remoteAddress));
                curlString.append(getUri(request));
                curlString.append("'");
                if (!hasDefaultMethod(request)) {
                    curlString.append(" -X ").append(request.getMethod().getValue());
                }
                for (Header header : request.getHeaderList()) {
                    for (NottableString headerValue : header.getValues()) {
                        curlString.append(" -H '").append(header.getName().getValue()).append(": ").append(headerValue.getValue()).append("'");
                        if (header.getName().getValue().toLowerCase().contains("Accept-Encoding".toLowerCase())) {
                            if (headerValue.getValue().toLowerCase().contains("gzip")
                                || headerValue.getValue().toLowerCase().contains("deflate")
                                || headerValue.getValue().toLowerCase().contains("sdch")) {
                                curlString.append(" ");
                                curlString.append("--compress");
                            }
                        }
                    }
                }
                curlString.append(getCookieHeader(request));
                if (isNotBlank(request.getBodyAsString())) {
                    curlString.append(" --data '").append(request.getBodyAsString().replace("'", "\\'")).append("'");
                }
            } else {
                curlString.append("no host header or remote address specified");
            }
        } else {
            curlString.append("null HttpRequest");
        }
        return curlString.toString();
    }

    private boolean hasDefaultMethod(HttpRequest request) {
        return request.getMethod() == null || isBlank(request.getMethod().getValue()) || request.getMethod().getValue().equalsIgnoreCase("GET");
    }

    private String getUri(HttpRequest request) {
        String uri = new MockServerHttpRequestToFullHttpRequest(mockServerLogger).getURI(request);
        if (isBlank(uri)) {
            uri = "/";
        } else if (!startsWith(uri, "/")) {
            uri = "/" + uri;
        }
        return uri;
    }

    private String getHostAndPort(HttpRequest request, InetSocketAddress remoteAddress) {
        String host = request.getFirstHeader("Host");
        if (isBlank(host)) {
            host = remoteAddress.getHostName() + ":" + remoteAddress.getPort();
        }
        return host;
    }

    private String getCookieHeader(HttpRequest request) {
        List<Cookie> cookies = new ArrayList<Cookie>();
        for (org.mockserver.model.Cookie cookie : request.getCookieList()) {
            cookies.add(new DefaultCookie(cookie.getName().getValue(), cookie.getValue().getValue()));
        }
        if (cookies.size() > 0) {
            return " -H '" + COOKIE + ": " + ClientCookieEncoder.LAX.encode(cookies) + "'";
        } else {
            return "";
        }
    }
}
