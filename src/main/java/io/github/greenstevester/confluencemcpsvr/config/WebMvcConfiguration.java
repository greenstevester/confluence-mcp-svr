package io.github.greenstevester.confluencemcpsvr.config;

import io.github.greenstevester.confluencemcpsvr.monitoring.McpEndpointInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for monitoring interceptors
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    
    @Autowired
    private McpEndpointInterceptor mcpEndpointInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mcpEndpointInterceptor)
               .addPathPatterns("/mcp/**");
    }
}