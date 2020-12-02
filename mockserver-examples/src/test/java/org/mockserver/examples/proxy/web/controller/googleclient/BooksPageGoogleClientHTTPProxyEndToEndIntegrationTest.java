package org.mockserver.examples.proxy.web.controller.googleclient;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockserver.examples.proxy.configuration.RootConfiguration;
import org.mockserver.examples.proxy.web.configuration.WebMvcConfiguration;
import org.mockserver.examples.proxy.web.controller.BooksPageEndToEndIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author jamesdbloom
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
    @ContextConfiguration(
        classes = {
            RootConfiguration.class
        }
    ),
    @ContextConfiguration(
        classes = {
            WebMvcConfiguration.class
        }
    )
})
@ActiveProfiles(profiles = {"backend", "googleClientHttpProxy"})
public class BooksPageGoogleClientHTTPProxyEndToEndIntegrationTest extends BooksPageEndToEndIntegrationTest {

}
