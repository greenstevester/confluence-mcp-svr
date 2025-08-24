#!/usr/bin/env bash

# Stop script for Confluence MCP Server Docker containers
# Usage: ./docker/stop.sh [--clean]

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

# Parse arguments
CLEAN_VOLUMES=false
CLEAN_IMAGES=false

for arg in "$@"; do
    case $arg in
        --clean)
            CLEAN_VOLUMES=true
            shift
            ;;
        --clean-all)
            CLEAN_VOLUMES=true
            CLEAN_IMAGES=true
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --clean      Remove volumes after stopping"
            echo "  --clean-all  Remove volumes and images after stopping"
            echo "  -h, --help   Show this help message"
            exit 0
            ;;
        *)
            ;;
    esac
done

# Function to check if containers are running
check_containers() {
    if ! docker-compose ps --services | grep -q "confluence-mcp-server"; then
        echo -e "${YELLOW}No Confluence MCP Server containers are running${NC}"
        return 1
    fi
    return 0
}

# Function to stop containers gracefully
stop_containers() {
    echo -e "${GREEN}Stopping Confluence MCP Server containers...${NC}"
    
    # Send SIGTERM and wait for graceful shutdown
    echo "Sending shutdown signal to containers..."
    docker-compose stop --timeout 30
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Containers stopped gracefully${NC}"
    else
        echo -e "${YELLOW}Warning: Some containers may not have stopped cleanly${NC}"
    fi
}

# Function to remove containers
remove_containers() {
    echo "Removing stopped containers..."
    docker-compose down
    
    if [ "$CLEAN_VOLUMES" = true ]; then
        echo -e "${YELLOW}Removing volumes...${NC}"
        docker-compose down -v
        
        # List and remove named volumes
        VOLUMES=$(docker volume ls -q | grep "confluence-mcp" || true)
        if [ -n "$VOLUMES" ]; then
            echo "Removing Confluence MCP volumes:"
            echo "$VOLUMES"
            docker volume rm $VOLUMES 2>/dev/null || true
        fi
    fi
    
    echo -e "${GREEN}✓ Containers removed${NC}"
}

# Function to clean up images
clean_images() {
    if [ "$CLEAN_IMAGES" = true ]; then
        echo -e "${YELLOW}Removing Confluence MCP Server images...${NC}"
        
        # Remove project images
        docker images | grep "confluence-mcp-svr" | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true
        
        # Prune dangling images
        echo "Pruning dangling images..."
        docker image prune -f
        
        echo -e "${GREEN}✓ Images cleaned${NC}"
    fi
}

# Function to show cleanup summary
show_summary() {
    echo ""
    echo -e "${GREEN}Cleanup Summary:${NC}"
    echo "  ✓ Containers stopped and removed"
    
    if [ "$CLEAN_VOLUMES" = true ]; then
        echo "  ✓ Volumes removed"
    fi
    
    if [ "$CLEAN_IMAGES" = true ]; then
        echo "  ✓ Images removed"
    fi
    
    # Show remaining resources
    echo ""
    echo "Remaining Docker resources:"
    
    # Check for any remaining containers
    REMAINING_CONTAINERS=$(docker ps -a --filter "name=confluence-mcp" --format "{{.Names}}" | wc -l)
    if [ "$REMAINING_CONTAINERS" -gt 0 ]; then
        echo -e "${YELLOW}  Warning: $REMAINING_CONTAINERS Confluence MCP containers still exist${NC}"
        docker ps -a --filter "name=confluence-mcp" --format "table {{.Names}}\t{{.Status}}"
    fi
    
    # Check for any remaining volumes
    REMAINING_VOLUMES=$(docker volume ls -q | grep "confluence-mcp" | wc -l || echo "0")
    if [ "$REMAINING_VOLUMES" -gt 0 ]; then
        echo -e "${YELLOW}  Warning: $REMAINING_VOLUMES Confluence MCP volumes still exist${NC}"
        docker volume ls | grep "confluence-mcp"
    fi
    
    # Check for any remaining images
    REMAINING_IMAGES=$(docker images | grep "confluence-mcp-svr" | wc -l || echo "0")
    if [ "$REMAINING_IMAGES" -gt 0 ]; then
        echo -e "${YELLOW}  Note: $REMAINING_IMAGES Confluence MCP images still exist${NC}"
        docker images | grep "confluence-mcp-svr"
    fi
}

# Main execution
main() {
    echo -e "${GREEN}Confluence MCP Server - Stop Script${NC}"
    echo ""
    
    # Load environment if exists
    if [ -f .env ]; then
        export $(grep -v '^#' .env | xargs) 2>/dev/null || true
    fi
    
    # Check if containers exist
    if ! check_containers; then
        # Check if there are any stopped containers to clean
        if docker ps -a --filter "name=confluence-mcp" | grep -q "confluence-mcp"; then
            echo "Found stopped containers. Cleaning up..."
            remove_containers
            clean_images
        else
            echo "No containers to stop or clean."
        fi
    else
        # Stop running containers
        stop_containers
        remove_containers
        clean_images
    fi
    
    show_summary
    
    echo ""
    echo -e "${GREEN}Done!${NC}"
}

# Handle script interruption
trap 'echo -e "\n${YELLOW}Interrupted${NC}"; exit 130' INT TERM

# Run main function
main