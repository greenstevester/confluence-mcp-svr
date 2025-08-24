package io.github.greenstevester.confluencemcpsvr.mcp;

import io.github.greenstevester.confluencemcpsvr.annotation.AITool;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for discovering and managing @AITool annotated methods
 */
@Component
public class AIToolRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(AIToolRegistry.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private final Map<String, AIToolDefinition> tools = new ConcurrentHashMap<>();
    
    /**
     * Represents a discovered AI tool
     */
    public static class AIToolDefinition {
        private final String name;
        private final String description;
        private final Method method;
        private final Object bean;
        private final AITool annotation;
        private final McpSchema.Tool mcpTool;
        
        public AIToolDefinition(String name, String description, Method method, Object bean, AITool annotation) {
            this.name = name;
            this.description = description;
            this.method = method;
            this.bean = bean;
            this.annotation = annotation;
            this.mcpTool = createMcpTool();
        }
        
        private McpSchema.Tool createMcpTool() {
            // Create parameter schema from method parameters
            Map<String, Object> properties = new LinkedHashMap<>();
            List<String> required = new ArrayList<>();
            
            for (Parameter param : method.getParameters()) {
                String paramName = param.getName();
                properties.put(paramName, Map.of(
                    "type", getJsonSchemaType(param.getType()),
                    "description", "Parameter " + paramName + " of type " + param.getType().getSimpleName()
                ));
                required.add(paramName);
            }
            
            McpSchema.JsonSchema inputSchema = new McpSchema.JsonSchema(
                "object",
                properties,
                required,
                null, // additionalProperties
                null, // definitions
                null  // items
            );
            
            return new McpSchema.Tool(name, description, inputSchema);
        }
        
        private String getJsonSchemaType(Class<?> type) {
            if (String.class.isAssignableFrom(type)) return "string";
            if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) return "integer";
            if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) return "boolean";
            if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) return "number";
            if (List.class.isAssignableFrom(type)) return "array";
            return "object";
        }
        
        // Getters
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Method getMethod() { return method; }
        public Object getBean() { return bean; }
        public AITool getAnnotation() { return annotation; }
        public McpSchema.Tool getMcpTool() { return mcpTool; }
    }
    
    @PostConstruct
    public void discoverTools() {
        logger.info("Starting @AITool discovery...");
        
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        int toolCount = 0;
        
        for (String beanName : beanNames) {
            try {
                Object bean = applicationContext.getBean(beanName);
                Class<?> beanClass = bean.getClass();
                
                // Skip Spring internal classes and proxies
                if (isInternalSpringClass(beanClass)) {
                    continue;
                }
                
                Method[] methods = beanClass.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(AITool.class)) {
                        AITool annotation = method.getAnnotation(AITool.class);
                        String toolName = annotation.name().isEmpty() ? method.getName() : annotation.name();
                        String description = annotation.description().isEmpty() ? method.getName() : annotation.description();
                        
                        AIToolDefinition toolDef = new AIToolDefinition(toolName, description, method, bean, annotation);
                        tools.put(toolName, toolDef);
                        
                        logger.info("Registered @AITool: {} - {} (method: {}.{})", 
                                   toolName, description, beanClass.getSimpleName(), method.getName());
                        toolCount++;
                    }
                }
            } catch (Exception e) {
                logger.debug("Skipping bean {} due to error: {}", beanName, e.getMessage());
            }
        }
        
        logger.info("@AITool discovery completed. Found {} tools.", toolCount);
    }
    
    private boolean isInternalSpringClass(Class<?> clazz) {
        String className = clazz.getName();
        return className.startsWith("org.springframework.") ||
               className.startsWith("com.sun.proxy.") ||
               className.contains("$$SpringCGLIB$$") ||
               className.contains("$$EnhancerBySpringCGLIB$$");
    }
    
    /**
     * Get all discovered tools
     */
    public Collection<AIToolDefinition> getAllTools() {
        return new ArrayList<>(tools.values());
    }
    
    /**
     * Get tool by name
     */
    public AIToolDefinition getTool(String name) {
        return tools.get(name);
    }
    
    /**
     * Get all MCP tool definitions
     */
    public List<McpSchema.Tool> getMcpTools() {
        return tools.values().stream()
                .map(AIToolDefinition::getMcpTool)
                .toList();
    }
    
    /**
     * Execute a tool by name
     */
    public Object executeTool(String toolName, Map<String, Object> parameters) throws Exception {
        AIToolDefinition toolDef = tools.get(toolName);
        if (toolDef == null) {
            throw new IllegalArgumentException("Tool not found: " + toolName);
        }
        
        Method method = toolDef.getMethod();
        Object bean = toolDef.getBean();
        
        // Convert parameters to method arguments
        Object[] args = convertParameters(method, parameters);
        
        // Make method accessible and invoke
        method.setAccessible(true);
        return method.invoke(bean, args);
    }
    
    private Object[] convertParameters(Method method, Map<String, Object> parameters) {
        Parameter[] methodParams = method.getParameters();
        Object[] args = new Object[methodParams.length];
        
        for (int i = 0; i < methodParams.length; i++) {
            Parameter param = methodParams[i];
            String paramName = param.getName();
            Object value = parameters.get(paramName);
            
            if (value != null) {
                args[i] = convertParameterValue(value, param.getType());
            }
        }
        
        return args;
    }
    
    private Object convertParameterValue(Object value, Class<?> targetType) {
        if (targetType.isInstance(value)) {
            return value;
        }
        
        // Handle common type conversions
        if (targetType == String.class) {
            return value.toString();
        } else if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else {
                return Integer.parseInt(value.toString());
            }
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Boolean) {
                return value;
            } else {
                return Boolean.parseBoolean(value.toString());
            }
        }
        
        return value;
    }
}