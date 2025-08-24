package io.github.greenstevester.confluencemcpsvr.model.dto;

import io.github.greenstevester.confluencemcpsvr.model.enums.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request object for creating a new Confluence page
 */
public record CreatePageRequest(
    @NotBlank(message = "Page title is required")
    String title,
    
    @NotBlank(message = "Space key is required")
    String spaceKey,
    
    String parentId,
    
    @NotNull(message = "Content is required")
    String content,
    
    String contentRepresentation,
    
    ContentStatus status
) {
    
    public CreatePageRequest {
        if (contentRepresentation == null) {
            contentRepresentation = "storage";
        }
        if (status == null) {
            status = ContentStatus.CURRENT;
        }
    }
    
    /**
     * Builder pattern for easier construction
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String title;
        private String spaceKey;
        private String parentId;
        private String content;
        private String contentRepresentation = "storage";
        private ContentStatus status = ContentStatus.CURRENT;
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder spaceKey(String spaceKey) {
            this.spaceKey = spaceKey;
            return this;
        }
        
        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder contentRepresentation(String contentRepresentation) {
            this.contentRepresentation = contentRepresentation;
            return this;
        }
        
        public Builder status(ContentStatus status) {
            this.status = status;
            return this;
        }
        
        public CreatePageRequest build() {
            return new CreatePageRequest(title, spaceKey, parentId, content, contentRepresentation, status);
        }
    }
}