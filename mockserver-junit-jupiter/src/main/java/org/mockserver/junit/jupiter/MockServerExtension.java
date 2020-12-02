package org.mockserver.junit.jupiter;

import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MockServerExtension implements ParameterResolver, BeforeAllCallback, AfterAllCallback {
    private static ClientAndServer perTestSuiteClient;
    private final ClientAndServer clientAndServer;
    private ClientAndServer client;
    private boolean perTestSuite;

    public MockServerExtension() {
        this.clientAndServer = new ClientAndServer();
    }

    public MockServerExtension(ClientAndServer clientAndServer) {
        this.clientAndServer = clientAndServer;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return MockServerClient.class.isAssignableFrom(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return client;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        List<Integer> ports = new ArrayList<>();
        Optional<MockServerSettings> mockServerSettingsOptional = retrieveAnnotationFromTestClass(context);
        if (mockServerSettingsOptional.isPresent()) {
            MockServerSettings mockServerSettings = mockServerSettingsOptional.get();
            perTestSuite = mockServerSettings.perTestSuite();
            for (int port : mockServerSettings.ports()) {
                ports.add(port);
            }
        }
        client = instantiateClient(ports);
    }

    private ClientAndServer instantiateClient(List<Integer> ports) {
        if (perTestSuite) {
            if (perTestSuiteClient == null) {
                perTestSuiteClient = ClientAndServer.startClientAndServer(ports);
                Runtime.getRuntime().addShutdownHook(new Scheduler.SchedulerThreadFactory("MockServer Test Extension ShutdownHook").newThread(() -> perTestSuiteClient.stop()));
            }
            return perTestSuiteClient;
        }
        return ClientAndServer.startClientAndServer(ports);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        if (!perTestSuite && client.isRunning()) {
            client.stop();
        }
    }

    private Optional<MockServerSettings> retrieveAnnotationFromTestClass(final ExtensionContext context) {
        ExtensionContext currentContext = context;
        Optional<MockServerSettings> annotation;

        do {
            annotation = AnnotationSupport.findAnnotation(currentContext.getElement(), MockServerSettings.class);
            if (!currentContext.getParent().isPresent()) {
                break;
            }
            currentContext = currentContext.getParent().get();
        } while (!annotation.isPresent() && currentContext != context.getRoot());

        return annotation;
    }
}
