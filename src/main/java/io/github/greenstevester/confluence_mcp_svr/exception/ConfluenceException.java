package io.github.greenstevester.confluence_mcp_svr.exception;

/**
 * Base exception for Confluence-related errors
 */
public class ConfluenceException extends RuntimeException {
    
    public ConfluenceException(String message) {
        super(message);
    }
    
    public ConfluenceException(String message, Throwable cause) {
        super(message, cause);
    }
}