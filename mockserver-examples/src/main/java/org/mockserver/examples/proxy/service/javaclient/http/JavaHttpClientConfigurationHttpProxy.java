package org.mockserver.examples.proxy.service.javaclient.http;

import org.mockserver.examples.proxy.service.BookService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * 此配置包含顶级bean和过滤器所需的任何配置
 *
 * 对Java发送的HTTP请求进行代理
 * @author jamesdbloom
 */
@Configuration
@Profile("javaClientHttpProxy")
@PropertySource({"classpath:application.properties"})
public class JavaHttpClientConfigurationHttpProxy {

    @Bean
    BookService bookService() {
        return new BookServiceJavaHttpClient();
    }
}
