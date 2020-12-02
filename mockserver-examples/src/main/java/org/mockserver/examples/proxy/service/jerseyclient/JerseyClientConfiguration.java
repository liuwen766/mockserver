package org.mockserver.examples.proxy.service.jerseyclient;

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
@Profile("jerseyClient")
@PropertySource({"classpath:application.properties"})
public class JerseyClientConfiguration {

    @Bean
    BookService bookService() {
        return new BookServiceJerseyClient();
    }
}
