package io.github.greenstevester.confluencemcpsvr.exception;

/**
 * Exception thrown when Confluence authentication fails
 */
public class ConfluenceAuthenticationException extends ConfluenceException {
    
    public ConfluenceAuthenticationException(String message) {
        super(message);
    }
    
    public ConfluenceAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}