package io.github.greenstevester.confluencemcpsvr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

/**
 * Debug component to show all registered endpoints at startup
 */
@Component
public class EndpointDebugger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(EndpointDebugger.class);
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RESET = "\u001B[0m";

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private Environment environment;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Skip console output in STDIO mode to prevent interference with MCP protocol
        if (!isStdioMode()) {
            debugRegisteredEndpoints();
        }
    }
    
    private boolean isStdioMode() {
        return environment.matchesProfiles("stdio") || 
               Boolean.parseBoolean(environment.getProperty("spring.ai.mcp.server.stdio", "false"));
    }

    private void debugRegisteredEndpoints() {
        System.out.println();
        System.out.println(ANSI_CYAN + "🔍 Debug: All Registered Endpoints" + ANSI_RESET);
        
        try {
            RequestMappingHandlerMapping mapping = applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();
            
            System.out.println("   Total endpoints found: " + handlerMethods.size());
            
            handlerMethods.forEach((info, method) -> {
                String patterns = info.getPatternsCondition() != null ? 
                    info.getPatternsCondition().getPatterns().toString() : "[]";
                String methods = info.getMethodsCondition().getMethods().toString();
                String className = method.getBeanType().getSimpleName();
                String methodName = method.getMethod().getName();
                
                System.out.println("   ├─ " + ANSI_YELLOW + patterns + ANSI_RESET + 
                    " " + methods + " → " + className + "." + methodName + "()");
            });
            
            // Check for MCP-specific endpoints
            boolean mcpFound = handlerMethods.keySet().stream()
                .anyMatch(info -> info.getPatternsCondition() != null &&
                    info.getPatternsCondition().getPatterns().stream()
                        .anyMatch(pattern -> pattern.contains("mcp")));
                        
            if (mcpFound) {
                System.out.println("   └─ ✅ MCP endpoints detected!");
            } else {
                System.out.println("   └─ ❌ No MCP endpoints found - this explains the 404!");
            }
            
        } catch (Exception e) {
            System.out.println("   └─ Error getting endpoints: " + e.getMessage());
        }
        
        System.out.println();
    }
}