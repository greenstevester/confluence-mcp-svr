#!/usr/bin/env bash

# Build script for Confluence MCP Server Docker image
# Usage: ./docker/build.sh [dev|prod] [--no-cache]

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Change to project root
cd "$PROJECT_ROOT"

# Default values
BUILD_ENV="${1:-dev}"
NO_CACHE=""
IMAGE_NAME="confluence-mcp-svr"
IMAGE_TAG="latest"

# Parse arguments
for arg in "$@"; do
    case $arg in
        --no-cache)
            NO_CACHE="--no-cache"
            shift
            ;;
        dev|prod)
            BUILD_ENV="$arg"
            shift
            ;;
        *)
            ;;
    esac
done

# Load environment variables if .env exists
if [ -f .env ]; then
    echo -e "${GREEN}Loading environment variables from .env file...${NC}"
    export $(grep -v '^#' .env | xargs)
fi

# Determine which Dockerfile to use
if [ "$BUILD_ENV" = "prod" ]; then
    DOCKERFILE="Dockerfile.prod"
    IMAGE_TAG="prod"
    echo -e "${YELLOW}Building PRODUCTION image...${NC}"
else
    DOCKERFILE="Dockerfile"
    IMAGE_TAG="dev"
    echo -e "${YELLOW}Building DEVELOPMENT image...${NC}"
fi

# Function to print build information
print_build_info() {
    echo "========================================"
    echo "Build Configuration:"
    echo "  Environment: $BUILD_ENV"
    echo "  Dockerfile: $DOCKERFILE"
    echo "  Image: $IMAGE_NAME:$IMAGE_TAG"
    echo "  No Cache: ${NO_CACHE:-false}"
    echo "  Build Context: $PROJECT_ROOT"
    echo "========================================"
}

# Function to check prerequisites
check_prerequisites() {
    echo -e "${GREEN}Checking prerequisites...${NC}"
    
    # Check if Docker is installed
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}Error: Docker is not installed${NC}"
        exit 1
    fi
    
    # Check if Docker daemon is running
    if ! docker info &> /dev/null; then
        echo -e "${RED}Error: Docker daemon is not running${NC}"
        exit 1
    fi
    
    # Check if Dockerfile exists
    if [ ! -f "$SCRIPT_DIR/$DOCKERFILE" ]; then
        echo -e "${RED}Error: $SCRIPT_DIR/$DOCKERFILE not found${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}Prerequisites check passed${NC}"
}

# Function to build the Docker image
build_image() {
    echo -e "${GREEN}Starting Docker build...${NC}"
    
    # Build command with BuildKit enabled for better caching
    DOCKER_BUILDKIT=1 docker build \
        $NO_CACHE \
        --build-arg BUILDKIT_INLINE_CACHE=1 \
        -f "$SCRIPT_DIR/$DOCKERFILE" \
        -t "$IMAGE_NAME:$IMAGE_TAG" \
        -t "$IMAGE_NAME:latest" \
        --progress=plain \
        "$PROJECT_ROOT"
    
    BUILD_STATUS=$?
    
    if [ $BUILD_STATUS -eq 0 ]; then
        echo -e "${GREEN}✓ Docker image built successfully!${NC}"
        
        # Show image details
        echo ""
        echo "Image details:"
        docker images "$IMAGE_NAME" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"
        
        # Get and display image layers
        echo ""
        echo "Image layers summary:"
        docker history "$IMAGE_NAME:$IMAGE_TAG" --format "table {{.CreatedBy}}\t{{.Size}}" | head -10
        
        # Calculate total size
        TOTAL_SIZE=$(docker images "$IMAGE_NAME:$IMAGE_TAG" --format "{{.Size}}")
        echo ""
        echo -e "${GREEN}Total image size: $TOTAL_SIZE${NC}"
    else
        echo -e "${RED}✗ Docker build failed with status $BUILD_STATUS${NC}"
        exit $BUILD_STATUS
    fi
}

# Function to run post-build tasks
post_build() {
    echo ""
    echo -e "${GREEN}Post-build tasks:${NC}"
    
    # Prune dangling images
    echo "Cleaning up dangling images..."
    docker image prune -f
    
    # Show next steps
    echo ""
    echo -e "${GREEN}Build complete! Next steps:${NC}"
    echo "  1. Run the container: ./docker/run.sh $BUILD_ENV"
    echo "  2. View logs: ./docker/logs.sh"
    echo "  3. Stop container: ./docker/stop.sh"
    
    if [ "$BUILD_ENV" = "dev" ]; then
        echo "  4. Debug the application: ./docker/debug.sh"
    fi
}

# Main execution
main() {
    echo -e "${GREEN}Confluence MCP Server - Docker Build Script${NC}"
    echo ""
    
    print_build_info
    echo ""
    
    check_prerequisites
    echo ""
    
    build_image
    
    post_build
}

# Run main function
main