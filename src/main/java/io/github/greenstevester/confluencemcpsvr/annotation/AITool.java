package io.github.greenstevester.confluencemcpsvr.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to mark methods as AI tools for MCP server auto-discovery.
 * This annotation provides our own tool registration mechanism.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AITool {
    
    /**
     * The name of the tool. If not specified, the method name will be used.
     */
    String name() default "";
    
    /**
     * The description of the tool. This is important for the AI model to understand
     * what the tool does and how to use it.
     */
    String description() default "";
    
    /**
     * Whether the tool result should be returned directly to the client 
     * or passed back to the model. Default is false (pass back to model).
     */
    boolean returnDirect() default false;
}