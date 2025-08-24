package io.github.greenstevester.confluencemcpsvr.model.dto;

import io.github.greenstevester.confluencemcpsvr.model.enums.SpaceStatus;
import io.github.greenstevester.confluencemcpsvr.model.enums.SpaceType;
import jakarta.validation.constraints.NotBlank;

/**
 * Request object for updating an existing Confluence space
 */
public record UpdateSpaceRequest(
    @NotBlank(message = "Space key is required")
    String spaceKey,
    
    String name,
    
    String description,
    
    SpaceType type,
    
    SpaceStatus status
) {
    
    /**
     * Builder pattern for easier construction
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String spaceKey;
        private String name;
        private String description;
        private SpaceType type;
        private SpaceStatus status;
        
        public Builder spaceKey(String spaceKey) {
            this.spaceKey = spaceKey;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder type(SpaceType type) {
            this.type = type;
            return this;
        }
        
        public Builder status(SpaceStatus status) {
            this.status = status;
            return this;
        }
        
        public UpdateSpaceRequest build() {
            return new UpdateSpaceRequest(spaceKey, name, description, type, status);
        }
    }
}