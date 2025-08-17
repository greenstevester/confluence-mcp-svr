package io.github.greenstevester.confluence_mcp_svr.exception;

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