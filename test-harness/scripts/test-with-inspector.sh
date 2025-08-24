#!/usr/bin/env bash

# MCP Inspector Test Script for Confluence MCP Server
# This script sets up and runs the MCP Inspector to test the server interactively

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"

echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Confluence MCP Server - Inspector     ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo ""

cd "$PROJECT_ROOT"

# Check if .env file exists
if [ ! -f .env ]; then
    echo -e "${RED}Error: .env file not found${NC}"
    echo "Please create a .env file with your Confluence API credentials"
    exit 1
fi

# Load environment variables
echo -e "${GREEN}Loading environment variables...${NC}"
set -a
source .env
set +a

# Validate required environment variables
MISSING_VARS=()
[ -z "${CONFLUENCE_API_BASE_URL:-}" ] && MISSING_VARS+=("CONFLUENCE_API_BASE_URL")
[ -z "${CONFLUENCE_API_USERNAME:-}" ] && MISSING_VARS+=("CONFLUENCE_API_USERNAME")
[ -z "${CONFLUENCE_API_TOKEN:-}" ] && MISSING_VARS+=("CONFLUENCE_API_TOKEN")

if [ ${#MISSING_VARS[@]} -gt 0 ]; then
    echo -e "${RED}Error: Missing required environment variables:${NC}"
    printf '%s\n' "${MISSING_VARS[@]}"
    exit 1
fi

# Build the server
echo -e "${GREEN}Building the server...${NC}"
./gradlew build -x test || {
    echo -e "${RED}Build failed. Please fix build errors and try again.${NC}"
    exit 1
}

# Get the JAR file path
JAR_FILE=$(find build/libs -name "*.jar" | grep -v plain | head -n1)
if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}Error: Could not find built JAR file${NC}"
    exit 1
fi

echo -e "${GREEN}Found JAR file: $JAR_FILE${NC}"

# Test mode selection
echo ""
echo -e "${BLUE}Select test transport mode:${NC}"
echo "1) STDIO mode (recommended for development)"
echo "2) SSE mode (WebMVC server on port 8081)"
echo ""
read -p "Select mode [1-2]: " mode_choice

case $mode_choice in
    1)
        echo -e "${GREEN}Testing with STDIO transport...${NC}"
        echo ""
        echo -e "${YELLOW}Starting MCP Inspector with STDIO transport...${NC}"
        echo "The Inspector will connect to the server via STDIO."
        echo ""
        
        npx @modelcontextprotocol/inspector \
            java \
            -Dspring.ai.mcp.server.stdio=true \
            -Dspring.main.web-application-type=none \
            -Dspring.main.banner-mode=off \
            -Dlogging.pattern.console= \
            -jar "$JAR_FILE"
        ;;
    2)
        echo -e "${GREEN}Testing with SSE transport...${NC}"
        echo ""
        echo "Starting the MCP server..."
        
        # Start the server in the background
        java -jar "$JAR_FILE" &
        SERVER_PID=$!
        
        # Wait for server to start
        echo "Waiting for server to start..."
        sleep 10
        
        # Check if server is running
        if ! curl -f http://localhost:8081/actuator/health > /dev/null 2>&1; then
            echo -e "${RED}Error: Server failed to start or is not healthy${NC}"
            kill $SERVER_PID 2>/dev/null || true
            exit 1
        fi
        
        echo -e "${GREEN}Server is running at http://localhost:8081${NC}"
        echo ""
        echo -e "${YELLOW}Starting MCP Inspector with SSE transport...${NC}"
        echo "The Inspector will connect to http://localhost:8081/mcp/message"
        echo ""
        
        # Note: MCP Inspector doesn't directly support SSE connections to running servers
        # Instead, we'll provide instructions for manual testing
        echo -e "${BLUE}Manual SSE Testing Instructions:${NC}"
        echo "1. Open your browser to test SSE endpoint: http://localhost:8081/mcp/message"
        echo "2. Use curl to test the endpoint:"
        echo "   curl -H 'Accept: text/event-stream' http://localhost:8081/mcp/message"
        echo ""
        echo "3. For programmatic testing, use the Java MCP client test utilities"
        echo "   (see test-harness/scripts/run-integration-tests.sh)"
        echo ""
        echo -e "${YELLOW}Press Ctrl+C to stop the server${NC}"
        
        # Keep server running
        wait $SERVER_PID
        ;;
    *)
        echo -e "${YELLOW}Invalid choice. Defaulting to STDIO mode.${NC}"
        npx @modelcontextprotocol/inspector \
            java \
            -Dspring.ai.mcp.server.stdio=true \
            -Dspring.main.web-application-type=none \
            -Dspring.main.banner-mode=off \
            -Dlogging.pattern.console= \
            -jar "$JAR_FILE"
        ;;
esac