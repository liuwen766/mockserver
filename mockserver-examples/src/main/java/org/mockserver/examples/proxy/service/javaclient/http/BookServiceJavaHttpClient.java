package org.mockserver.examples.proxy.service.javaclient.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockserver.examples.proxy.model.Book;
import org.mockserver.examples.proxy.service.BookService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import static org.mockserver.examples.proxy.json.ObjectMapperFactory.createObjectMapper;

/**
 * @author jamesdbloom
 */
@Component
public class BookServiceJavaHttpClient implements BookService {

    @Resource
    private Environment environment;
    private Integer port;
    private String host;
    private ObjectMapper objectMapper;

    //初始化环境
    @PostConstruct
    private void initialise() {
        port = Integer.parseInt(System.getProperty("bookService.port"));
        host = environment.getProperty("bookService.host", "localhost");
        objectMapper = createObjectMapper();
    }

    //通过代理发送请求
    private HttpURLConnection sendRequestViaProxy(URL url) throws IOException {
        System.out.println(System.getProperty("http.proxyHost")+"***"+System.getProperty("http.proxyPort"));
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort"))));
        return (HttpURLConnection) url.openConnection(proxy);
    }

    //通过代理发送请求并获取响应
    @Override
    public Book[] getAllBooks() {
        try {
            logger.info("发送请求:http://" + host + ":" + port + "/get_books");
            HttpURLConnection connection = sendRequestViaProxy(new URL("http://" + host + ":" + port + "/get_books"));
            connection.setRequestMethod("GET");
            return objectMapper.readValue(connection.getInputStream(), Book[].class);
        } catch (Exception e) {
            logger.info("Exception 发送请求: http://" + host + ":" + port + "/get_books", e);
            throw new RuntimeException("Exception making request to retrieve all books", e);
        }
    }

    @Override
    public Book getBook(String id) {
        try {
            logger.info("发送请求: http://" + host + ":" + port + "/get_book?id=" + id);
            HttpURLConnection connection = sendRequestViaProxy(new URL("http://" + host + ":" + port + "/get_book?id=" + id));
            connection.setRequestMethod("GET");
            return objectMapper.readValue(connection.getInputStream(), Book.class);
        } catch (Exception e) {
            logger.info("Exception sending request to http://" + host + ":" + port + "/get_books", e);
            throw new RuntimeException("Exception making request to retrieve a book with id [" + id + "]", e);
        }
    }
}
