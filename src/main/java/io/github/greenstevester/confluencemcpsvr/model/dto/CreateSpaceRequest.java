package io.github.greenstevester.confluencemcpsvr.model.dto;

import io.github.greenstevester.confluencemcpsvr.model.enums.SpaceStatus;
import io.github.greenstevester.confluencemcpsvr.model.enums.SpaceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request object for creating a new Confluence space
 */
public record CreateSpaceRequest(
    @NotBlank(message = "Space key is required")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Space key must contain only uppercase letters, numbers, and underscores")
    String key,
    
    @NotBlank(message = "Space name is required")
    String name,
    
    String description,
    
    SpaceType type,
    
    SpaceStatus status
) {
    
    public CreateSpaceRequest {
        if (type == null) {
            type = SpaceType.GLOBAL;
        }
        if (status == null) {
            status = SpaceStatus.CURRENT;
        }
    }
    
    /**
     * Builder pattern for easier construction
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String key;
        private String name;
        private String description;
        private SpaceType type = SpaceType.GLOBAL;
        private SpaceStatus status = SpaceStatus.CURRENT;
        
        public Builder key(String key) {
            this.key = key;
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
        
        public CreateSpaceRequest build() {
            return new CreateSpaceRequest(key, name, description, type, status);
        }
    }
}