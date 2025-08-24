package io.github.greenstevester.confluencemcpsvr.mcp;

import io.github.greenstevester.confluencemcpsvr.mcp.AIToolRegistry.AIToolDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ToolCallback implementation that wraps our custom @AITool methods
 */
public class AIToolCallback implements ToolCallback {
    
    private static final Logger logger = LoggerFactory.getLogger(AIToolCallback.class);
    
    private final AIToolDefinition toolDefinition;
    
    public AIToolCallback(AIToolDefinition toolDefinition) {
        this.toolDefinition = toolDefinition;
    }
    
    @Override
    public String call(String toolInput) {
        logger.debug("Executing @AITool: {} with input: {}", toolDefinition.getName(), toolInput);
        
        try {
            // Parse the JSON input to extract parameters
            Map<String, Object> parameters = parseJsonInput(toolInput);
            
            // Execute the tool using our registry
            Object result = executeToolMethod(parameters);
            
            // Convert result to string
            String resultString = result != null ? result.toString() : "null";
            
            logger.debug("@AITool {} executed successfully, result length: {}", 
                        toolDefinition.getName(), resultString.length());
            
            return resultString;
            
        } catch (Exception e) {
            logger.error("Error executing @AITool {}: {}", toolDefinition.getName(), e.getMessage(), e);
            return "Error executing tool: " + e.getMessage();
        }
    }
    
    @Override
    public ToolDefinition getToolDefinition() {
        // Create a simple ToolDefinition implementation
        return new ToolDefinition() {
            @Override
            public String name() {
                return toolDefinition.getName();
            }
            
            @Override
            public String description() {
                return toolDefinition.getDescription();
            }
            
            @Override
            public String inputSchema() {
                // Return a simple object schema
                return "{\"type\":\"object\",\"properties\":{}}";
            }
        };
    }
    
    private Map<String, Object> parseJsonInput(String toolInput) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        
        if (toolInput == null || toolInput.trim().isEmpty()) {
            return parameters;
        }
        
        // Simple JSON parsing for our use case
        // In a production system, you'd want to use a proper JSON parser
        try {
            String input = toolInput.trim();
            if (input.startsWith("{") && input.endsWith("}")) {
                input = input.substring(1, input.length() - 1); // Remove { }
                
                String[] pairs = input.split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":", 2);
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replaceAll("\"", "");
                        String value = keyValue[1].trim().replaceAll("\"", "");
                        parameters.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse JSON input: {}, using empty parameters", toolInput);
        }
        
        return parameters;
    }
    
    private Object executeToolMethod(Map<String, Object> parameters) throws Exception {
        Method method = toolDefinition.getMethod();
        Object bean = toolDefinition.getBean();
        
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