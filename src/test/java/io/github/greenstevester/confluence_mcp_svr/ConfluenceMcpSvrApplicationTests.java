package io.github.greenstevester.confluence_mcp_svr;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "confluence.api.base-url=https://test.atlassian.net",
    "confluence.api.username=test@example.com", 
    "confluence.api.token=test-token",
    "confluence.api.timeout=30s",
    "confluence.api.max-connections=20",
    "confluence.api.retry-attempts=3",
    "confluence.defaults.page-size=25",
    "confluence.defaults.body-format=storage",
    "confluence.defaults.include-labels=true",
    "confluence.defaults.include-properties=false",
    "confluence.defaults.include-webresources=false",
    "confluence.defaults.include-collaborators=false",
    "confluence.defaults.include-version=true",
    "mcp.server.name=test-server",
    "mcp.server.version=2.0.1",
    "mcp.server.description=Test server"
})
class ConfluenceMcpSvrApplicationTests {

	@Test
	void contextLoads() {
	}

}
