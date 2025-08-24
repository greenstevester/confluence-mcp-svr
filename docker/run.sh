#!/usr/bin/env bash

# Run script for Confluence MCP Server Docker container
# Usage: ./docker/run.sh [dev|prod] [--detach]

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Change to project root
cd "$PROJECT_ROOT"

# Default values
RUN_ENV="${1:-dev}"
DETACH_MODE=""
COMPOSE_FILE="docker-compose.yml"
ADDITIONAL_COMPOSE=""

# Parse arguments
for arg in "$@"; do
    case $arg in
        --detach|-d)
            DETACH_MODE="-d"
            shift
            ;;
        dev|prod)
            RUN_ENV="$arg"
            shift
            ;;
        *)
            ;;
    esac
done

# Check if .env file exists
check_env_file() {
    if [ ! -f .env ]; then
        echo -e "${YELLOW}Warning: .env file not found${NC}"
        echo "Creating .env from .env.example..."
        
        if [ -f .env.example ]; then
            cp .env.example .env
            echo -e "${GREEN}Created .env file. Please update it with your Confluence API credentials.${NC}"
            echo -e "${RED}Exiting. Please configure your .env file before running.${NC}"
            exit 1
        else
            echo -e "${RED}Error: .env.example not found${NC}"
            exit 1
        fi
    fi
}

# Load environment variables
load_env() {
    echo -e "${GREEN}Loading environment variables...${NC}"
    set -a
    source .env
    set +a
    
    # Set environment-specific variables
    if [ "$RUN_ENV" = "prod" ]; then
        export SPRING_PROFILES_ACTIVE=prod
        export DOCKERFILE=Dockerfile.prod
    else
        export SPRING_PROFILES_ACTIVE=dev
        export DOCKERFILE=Dockerfile
        ADDITIONAL_COMPOSE="-f docker-compose.dev.yml"
    fi
}

# Validate required environment variables
validate_env() {
    echo -e "${GREEN}Validating configuration...${NC}"
    
    local MISSING_VARS=()
    
    # Check required variables
    [ -z "${ATLASSIAN_SITE_NAME:-}" ] && MISSING_VARS+=("ATLASSIAN_SITE_NAME")
    [ -z "${ATLASSIAN_USER_EMAIL:-}" ] && MISSING_VARS+=("ATLASSIAN_USER_EMAIL")
    [ -z "${ATLASSIAN_API_TOKEN:-}" ] && MISSING_VARS+=("ATLASSIAN_API_TOKEN")
    
    if [ ${#MISSING_VARS[@]} -gt 0 ]; then
        echo -e "${RED}Error: Missing required environment variables:${NC}"
        printf '%s\n' "${MISSING_VARS[@]}"
        echo ""
        echo "Please update your .env file with the required values."
        exit 1
    fi
    
    echo -e "${GREEN}✓ Configuration validated${NC}"
}

# Check if containers are already running
check_running_containers() {
    if docker-compose ps --services --filter "status=running" | grep -q "confluence-mcp-server"; then
        echo -e "${YELLOW}Container 'confluence-mcp-server' is already running${NC}"
        echo ""
        read -p "Do you want to restart it? (y/N): " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "Stopping existing container..."
            docker-compose down
            sleep 2
        else
            echo "Exiting without changes."
            exit 0
        fi
    fi
}

# Start the containers
start_containers() {
    echo -e "${GREEN}Starting Confluence MCP Server ($RUN_ENV mode)...${NC}"
    echo ""
    
    # Build the compose command
    COMPOSE_CMD="docker-compose -f $COMPOSE_FILE"
    if [ -n "$ADDITIONAL_COMPOSE" ]; then
        COMPOSE_CMD="$COMPOSE_CMD $ADDITIONAL_COMPOSE"
    fi
    
    # Pull latest base images if needed
    echo "Pulling latest base images..."
    $COMPOSE_CMD pull --ignore-pull-failures
    
    # Start containers
    echo "Starting containers..."
    $COMPOSE_CMD up $DETACH_MODE --build --remove-orphans
    
    if [ -n "$DETACH_MODE" ]; then
        # Wait for container to be healthy
        wait_for_health
        show_status
    fi
}

# Wait for container to be healthy
wait_for_health() {
    echo -e "${YELLOW}Waiting for application to be healthy...${NC}"
    
    local MAX_ATTEMPTS=30
    local ATTEMPT=0
    
    while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
        if docker-compose ps | grep -q "healthy"; then
            echo -e "${GREEN}✓ Application is healthy!${NC}"
            return 0
        elif docker-compose ps | grep -q "unhealthy"; then
            echo -e "${RED}✗ Application is unhealthy${NC}"
            echo "Check logs with: ./docker/logs.sh"
            return 1
        fi
        
        ATTEMPT=$((ATTEMPT + 1))
        echo -n "."
        sleep 2
    done
    
    echo ""
    echo -e "${YELLOW}Health check timeout. Application may still be starting.${NC}"
    echo "Check status with: docker-compose ps"
}

# Show container status
show_status() {
    echo ""
    echo -e "${GREEN}Container Status:${NC}"
    docker-compose ps
    
    echo ""
    echo -e "${GREEN}Application URLs:${NC}"
    echo "  Application: http://localhost:${SERVER_PORT:-8081}"
    echo "  Health Check: http://localhost:${SERVER_PORT:-8081}/actuator/health"
    
    if [ "$RUN_ENV" = "dev" ]; then
        echo "  Debug Port: localhost:5005"
        echo "  LiveReload: localhost:35729"
    fi
    
    echo ""
    echo -e "${GREEN}Useful commands:${NC}"
    echo "  View logs: ./docker/logs.sh"
    echo "  Stop containers: ./docker/stop.sh"
    echo "  Shell into container: docker exec -it confluence-mcp-server sh"
    
    if [ "$RUN_ENV" = "dev" ]; then
        echo "  Debug: ./docker/debug.sh"
    fi
}

# Main execution
main() {
    echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║  Confluence MCP Server - Run Script    ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
    echo ""
    
    check_env_file
    load_env
    validate_env
    check_running_containers
    start_containers
}

# Handle script interruption
trap 'echo -e "\n${YELLOW}Interrupted. Stopping containers...${NC}"; docker-compose down; exit 130' INT TERM

# Run main function
main