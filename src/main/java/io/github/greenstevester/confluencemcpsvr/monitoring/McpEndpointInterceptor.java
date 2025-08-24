package io.github.greenstevester.confluencemcpsvr.monitoring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Intercepts MCP endpoint requests to track usage statistics
 */
@Component
public class McpEndpointInterceptor implements HandlerInterceptor {
    
    @Autowired
    private McpEndpointTracker endpointTracker;
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) throws Exception {
        
        String requestURI = request.getRequestURI();
        
        // Track MCP endpoints
        if (requestURI.startsWith("/mcp/")) {
            long contentLength = response.getHeader("Content-Length") != null 
                ? Long.parseLong(response.getHeader("Content-Length"))
                : estimateResponseSize(response);
                
            endpointTracker.recordEndpointHit(requestURI, contentLength);
        }
    }
    
    private long estimateResponseSize(HttpServletResponse response) {
        // Estimate response size based on status and common response patterns
        int status = response.getStatus();
        if (status >= 400) {
            return 200; // Error responses are typically small
        } else if (status == 200) {
            return 1024; // Assume 1KB for successful responses without explicit length
        }
        return 100; // Default small size
    }
}