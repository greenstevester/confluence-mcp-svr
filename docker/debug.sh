#!/usr/bin/env bash

# Debug script for Confluence MCP Server Docker container
# Usage: ./docker/debug.sh [--port PORT] [--suspend]

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
DEBUG_PORT="5005"
SUSPEND="n"
CONTAINER_NAME="confluence-mcp-server"

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --port|-p)
            DEBUG_PORT="$2"
            shift 2
            ;;
        --suspend|-s)
            SUSPEND="y"
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  -p, --port PORT    Debug port (default: 5005)"
            echo "  -s, --suspend      Suspend JVM startup until debugger connects"
            echo "  -h, --help         Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                        # Start with default debug port 5005"
            echo "  $0 --port 8000           # Use custom debug port"
            echo "  $0 --suspend             # Wait for debugger before starting"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Function to check if container is running
check_container() {
    if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        echo -e "${YELLOW}Container '${CONTAINER_NAME}' is not running${NC}"
        echo ""
        echo "Starting container in debug mode..."
        start_debug_container
        return 0
    fi
    
    echo -e "${GREEN}Container '${CONTAINER_NAME}' is already running${NC}"
    
    # Check if debug port is exposed
    if docker port $CONTAINER_NAME | grep -q "$DEBUG_PORT"; then
        echo -e "${GREEN}Debug port $DEBUG_PORT is already exposed${NC}"
        return 0
    else
        echo -e "${YELLOW}Debug port is not exposed. Restarting container with debug configuration...${NC}"
        restart_with_debug
        return 0
    fi
}

# Function to start container in debug mode
start_debug_container() {
    echo -e "${GREEN}Starting Confluence MCP Server in debug mode...${NC}"
    
    # Load environment variables
    if [ -f .env ]; then
        export $(grep -v '^#' .env | xargs)
    fi
    
    # Set debug environment variables
    export SPRING_PROFILES_ACTIVE=dev
    export JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -agentlib:jdwp=transport=dt_socket,server=y,suspend=$SUSPEND,address=*:$DEBUG_PORT"
    
    # Start with debug compose configuration
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
    
    # Wait for container to be ready
    wait_for_container
}

# Function to restart container with debug enabled
restart_with_debug() {
    echo "Stopping current container..."
    docker-compose stop
    
    # Set debug environment variables
    export SPRING_PROFILES_ACTIVE=dev
    export JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -agentlib:jdwp=transport=dt_socket,server=y,suspend=$SUSPEND,address=*:$DEBUG_PORT"
    
    echo "Restarting with debug configuration..."
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
    
    wait_for_container
}

# Function to wait for container to be ready
wait_for_container() {
    echo -e "${YELLOW}Waiting for container to be ready...${NC}"
    
    local max_attempts=30
    local attempt=0
    
    while [ $attempt -lt $max_attempts ]; do
        if docker ps | grep -q "$CONTAINER_NAME.*Up.*healthy"; then
            echo -e "${GREEN}✓ Container is ready!${NC}"
            return 0
        fi
        
        attempt=$((attempt + 1))
        echo -n "."
        sleep 2
    done
    
    echo ""
    echo -e "${YELLOW}Container may still be starting. Check logs for details.${NC}"
}

# Function to test debug connection
test_debug_connection() {
    echo ""
    echo -e "${GREEN}Testing debug port availability...${NC}"
    
    if nc -z localhost $DEBUG_PORT 2>/dev/null; then
        echo -e "${GREEN}✓ Debug port $DEBUG_PORT is open and listening${NC}"
    else
        echo -e "${RED}✗ Debug port $DEBUG_PORT is not accessible${NC}"
        echo "Please check container logs for errors."
        return 1
    fi
}

# Function to show debug instructions
show_debug_instructions() {
    echo ""
    echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║                    Debug Configuration Ready                      ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${GREEN}Debug Information:${NC}"
    echo "  • Debug Port: $DEBUG_PORT"
    echo "  • Suspend on Start: $SUSPEND"
    echo "  • Container: $CONTAINER_NAME"
    echo ""
    
    echo -e "${GREEN}IDE Configuration:${NC}"
    echo ""
    echo "IntelliJ IDEA:"
    echo "  1. Run → Edit Configurations → Add New Configuration → Remote JVM Debug"
    echo "  2. Name: Confluence MCP Server Remote Debug"
    echo "  3. Host: localhost"
    echo "  4. Port: $DEBUG_PORT"
    echo "  5. Command line arguments: -agentlib:jdwp=transport=dt_socket,server=y,suspend=$SUSPEND,address=*:$DEBUG_PORT"
    echo "  6. Use module classpath: confluence-mcp-svr"
    echo ""
    
    echo "VS Code:"
    echo "  1. Add to launch.json:"
    echo '  {'
    echo '    "type": "java",'
    echo '    "name": "Debug Confluence MCP Server",'
    echo '    "request": "attach",'
    echo '    "hostName": "localhost",'
    echo "    \"port\": $DEBUG_PORT"
    echo '  }'
    echo ""
    
    echo "Eclipse:"
    echo "  1. Run → Debug Configurations → Remote Java Application → New"
    echo "  2. Project: confluence-mcp-svr"
    echo "  3. Connection Type: Standard (Socket Attach)"
    echo "  4. Host: localhost"
    echo "  5. Port: $DEBUG_PORT"
    echo ""
    
    if [ "$SUSPEND" = "y" ]; then
        echo -e "${YELLOW}⚠ Application is suspended and waiting for debugger connection${NC}"
        echo "  Connect your debugger now to continue application startup"
    else
        echo -e "${GREEN}Application is running and ready for debugger connection${NC}"
    fi
    
    echo ""
    echo -e "${GREEN}Other useful commands:${NC}"
    echo "  • View logs: ./docker/logs.sh --follow"
    echo "  • Shell into container: docker exec -it $CONTAINER_NAME sh"
    echo "  • Stop debugging: ./docker/stop.sh"
}

# Function to monitor debug session
monitor_debug_session() {
    echo ""
    echo -e "${GREEN}Monitoring debug session (Ctrl+C to exit)...${NC}"
    echo "----------------------------------------"
    
    # Show initial logs
    docker logs --tail 20 $CONTAINER_NAME
    
    # Follow logs with debug highlighting
    docker logs --follow $CONTAINER_NAME 2>&1 | \
        sed -E "s/(Listening for transport dt_socket at address: [0-9]+)/$(printf '\033[0;32m')\1$(printf '\033[0m')/g" | \
        sed -E "s/(JDWP|jdwp|Debug|debug)/$(printf '\033[0;34m')\1$(printf '\033[0m')/g"
}

# Main execution
main() {
    echo -e "${BLUE}Confluence MCP Server - Debug Mode${NC}"
    echo ""
    
    # Check and prepare container
    check_container
    
    # Test debug connection
    test_debug_connection
    
    # Show debug instructions
    show_debug_instructions
    
    # Ask if user wants to monitor logs
    echo ""
    read -p "Do you want to monitor debug logs? (y/N): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        monitor_debug_session
    fi
}

# Handle script interruption
trap 'echo -e "\n${YELLOW}Debug session monitoring stopped${NC}"; exit 0' INT TERM

# Run main function
main