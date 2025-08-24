package io.github.greenstevester.confluencemcpsvr.model.dto;

import io.github.greenstevester.confluencemcpsvr.model.enums.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request object for updating an existing Confluence page
 */
public record UpdatePageRequest(
    @NotBlank(message = "Page ID is required")
    String pageId,
    
    String title,
    
    String content,
    
    String contentRepresentation,
    
    ContentStatus status,
    
    @NotNull(message = "Version number is required for updates")
    Integer version,
    
    String parentId
) {
    
    // No custom constructor - rely on builder pattern for defaults
    
    /**
     * Builder pattern for easier construction
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String pageId;
        private String title;
        private String content;
        private String contentRepresentation = "storage";
        private ContentStatus status;
        private Integer version;
        private String parentId;
        
        public Builder pageId(String pageId) {
            this.pageId = pageId;
            return this;
        }
        
        public Builder title(String title) {
            this.title = title;
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
        
        public Builder version(Integer version) {
            this.version = version;
            return this;
        }
        
        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }
        
        public UpdatePageRequest build() {
            // Ensure contentRepresentation has a default value
            String finalContentRepresentation = contentRepresentation != null ? contentRepresentation : "storage";
            return new UpdatePageRequest(pageId, title, content, finalContentRepresentation, status, version, parentId);
        }
    }
}