package org.mockserver.examples.proxy.service.googleclient.http;

import org.mockserver.examples.proxy.service.BookService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * This configuration contains top level beans and any configuration required by filters (as WebMvcConfiguration only loaded within Dispatcher Servlet)
 *
 * @author jamesdbloom
 */
@Configuration
@Profile("googleClientHttpProxy")
@PropertySource({"classpath:application.properties"})
public class GoogleHttpClientConfigurationHttpProxy {

    @Bean
    BookService bookService() {
        return new BookServiceGoogleHttpClient();
    }
}
