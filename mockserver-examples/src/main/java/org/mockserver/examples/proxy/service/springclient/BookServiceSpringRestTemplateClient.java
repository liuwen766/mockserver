package org.mockserver.examples.proxy.service.springclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.mockserver.examples.proxy.model.Book;
import org.mockserver.examples.proxy.service.BookService;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static org.mockserver.examples.proxy.json.ObjectMapperFactory.createObjectMapper;

/**
 * @author jamesdbloom
 */
@Component
public class BookServiceSpringRestTemplateClient implements BookService {

    @Resource
    private Environment environment;
    private Integer port;
    private String host;
    private ObjectMapper objectMapper;
    private RestTemplate restTemplate;

    @PostConstruct
    private void initialise() {
        port = Integer.parseInt(System.getProperty("bookService.port"));
        host = environment.getProperty("bookService.host", "localhost");
        objectMapper = createObjectMapper();
        restTemplate = createRestTemplate();
    }

    private RestTemplate createRestTemplate() {
        // jackson message converter
        MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJacksonHttpMessageConverter.setObjectMapper(objectMapper);

        // create message converters list
        List<HttpMessageConverter<?>> httpMessageConverters = new ArrayList<HttpMessageConverter<?>>();
        httpMessageConverters.add(mappingJacksonHttpMessageConverter);

        // create rest template
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(httpMessageConverters);

        // configure proxy
        HttpHost httpHost = new HttpHost(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")), "http");
        DefaultProxyRoutePlanner defaultProxyRoutePlanner = new DefaultProxyRoutePlanner(httpHost);
        HttpClient httpClient = HttpClients.custom().setRoutePlanner(defaultProxyRoutePlanner).build();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));

        return restTemplate;
    }

    @Override
    public Book[] getAllBooks() {
        try {
            logger.info("Sending request to http://" + host + ":" + port + "/get_books");
            return restTemplate.getForObject("http://" + host + ":" + port + "/get_books", Book[].class);
        } catch (Exception e) {
            logger.info("Exception sending request to http://" + host + ":" + port + "/get_books", e);
            throw new RuntimeException("Exception making request to retrieve all books", e);
        }
    }

    @Override
    public Book getBook(String id) {
        try {
            logger.info("Sending request to http://" + host + ":" + port + "/get_book?id=" + id);
            return restTemplate.getForObject("http://" + host + ":" + port + "/get_book?id=" + id, Book.class);
        } catch (Exception e) {
            logger.info("Exception sending request to http://" + host + ":" + port + "/get_books", e);
            throw new RuntimeException("Exception making request to retrieve a book with id [" + id + "]", e);
        }
    }
}
