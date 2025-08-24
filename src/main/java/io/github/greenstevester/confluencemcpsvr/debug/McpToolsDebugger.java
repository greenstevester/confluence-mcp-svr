package io.github.greenstevester.confluencemcpsvr.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Debug component to check what @Tool annotated methods are available
 */
@Component
public class McpToolsDebugger implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(McpToolsDebugger.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("=== MCP Tools Discovery Debug ===");
        
        // Find all beans and check for @Tool methods
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        int toolCount = 0;
        
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            
            Method[] methods = beanClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Tool.class)) {
                    Tool toolAnnotation = method.getAnnotation(Tool.class);
                    logger.info("Found @Tool method: {}.{} - name: '{}', description: '{}'", 
                               beanClass.getSimpleName(), 
                               method.getName(),
                               toolAnnotation.name(),
                               toolAnnotation.description());
                    toolCount++;
                }
            }
        }
        
        logger.info("Total @Tool methods found: {}", toolCount);
        logger.info("=== End MCP Tools Discovery Debug ===");
    }
}