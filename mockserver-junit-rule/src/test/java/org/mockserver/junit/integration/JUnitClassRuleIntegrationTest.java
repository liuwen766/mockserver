package org.mockserver.junit.integration;

import org.junit.*;
import org.mockserver.testing.integration.mock.AbstractBasicMockingIntegrationTest;
import org.mockserver.junit.MockServerRule;
import org.mockserver.socket.PortFactory;

/**
 * @author jamesdbloom
 */
public class JUnitClassRuleIntegrationTest extends AbstractBasicMockingIntegrationTest {

    // used fixed port for rule for all tests to ensure MockServer has been fully shutdown between each test
    private static final int MOCK_SERVER_PORT = PortFactory.findFreePort();

    @ClassRule
    public static final MockServerRule mockServerRule = new MockServerRule(JUnitClassRuleIntegrationTest.class, MOCK_SERVER_PORT);

    @Before
    @Override
    public void resetServer() {
        mockServerRule.getClient().reset();
    }

    @Override
    public int getServerPort() {
        return mockServerRule.getPort();
    }

}
