package org.mockserver.examples.proxy.service.apacheclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.mockserver.examples.proxy.model.Book;
import org.mockserver.examples.proxy.service.BookService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

import static org.mockserver.examples.proxy.json.ObjectMapperFactory.createObjectMapper;

/**
 * @author jamesdbloom
 */
@Component
public class BookServiceApacheHttpClient implements BookService {

    @Resource
    private Environment environment;
    private Integer port;
    private String host;
    private ObjectMapper objectMapper;
    private HttpClient httpClient;

    @PostConstruct
    private void initialise() {
        port = Integer.parseInt(System.getProperty("bookService.port"));
        host = environment.getProperty("bookService.host", "localhost");
        objectMapper = createObjectMapper();
        httpClient = createHttpClient();
    }

    private HttpClient createHttpClient() {
        HttpHost httpHost = new HttpHost(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")), "http");
        HttpRoutePlanner defaultProxyRoutePlanner = new DefaultProxyRoutePlanner(httpHost);
        return HttpClients.custom().setRoutePlanner(defaultProxyRoutePlanner).build();
    }

    @Override
    public Book[] getAllBooks() {
        String responseBody;
        try {
            HttpResponse response = httpClient.execute(new HttpGet(new URIBuilder()
                .setScheme("http")
                .setHost(host)
                .setPort(port)
                .setPath("/get_books")
                .build()));
            responseBody = new String(EntityUtils.toByteArray(response.getEntity()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Exception making request to retrieve all books", e);
        }
        try {
            return objectMapper.readValue(responseBody, Book[].class);
        } catch (Exception e) {
            throw new RuntimeException("Exception parsing JSON response [" + responseBody + "]", e);
        }
    }

    @Override
    public Book getBook(String id) {
        try {
            HttpResponse response = httpClient.execute(new HttpGet(new URIBuilder()
                .setScheme("http")
                .setHost(host)
                .setPort(port)
                .setPath("/get_book")
                .setParameter("id", id)
                .build()));
            return objectMapper.readValue(EntityUtils.toByteArray(response.getEntity()), Book.class);
        } catch (Exception e) {
            throw new RuntimeException("Exception making request to retrieve a book with id [" + id + "]", e);
        }
    }
}
