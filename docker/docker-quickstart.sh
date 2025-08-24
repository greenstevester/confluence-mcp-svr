#!/usr/bin/env bash

# Quick start script for Confluence MCP Server
# This script provides an interactive setup for first-time users

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Change to project root
cd "$PROJECT_ROOT"

# Banner
show_banner() {
    echo -e "${CYAN}"
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║           Confluence MCP Server - Docker Setup             ║"
    echo "║                    Quick Start Wizard                      ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# Check prerequisites
check_prerequisites() {
    echo -e "${GREEN}Checking prerequisites...${NC}"
    
    local missing_tools=()
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        missing_tools+=("Docker")
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        if ! docker compose version &> /dev/null; then
            missing_tools+=("Docker Compose")
        fi
    fi
    
    if [ ${#missing_tools[@]} -gt 0 ]; then
        echo -e "${RED}Missing required tools:${NC}"
        printf '%s\n' "${missing_tools[@]}"
        echo ""
        echo "Please install the missing tools and run this script again."
        echo "Visit https://docs.docker.com/get-docker/ for installation instructions."
        exit 1
    fi
    
    # Check if Docker daemon is running
    if ! docker info &> /dev/null; then
        echo -e "${RED}Docker daemon is not running.${NC}"
        echo "Please start Docker and run this script again."
        exit 1
    fi
    
    echo -e "${GREEN}✓ All prerequisites met${NC}"
    echo ""
}

# Setup environment file
setup_environment() {
    echo -e "${BLUE}Environment Configuration${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    if [ -f .env ]; then
        echo -e "${YELLOW}An .env file already exists.${NC}"
        read -p "Do you want to reconfigure it? (y/N): " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo "Using existing configuration."
            return 0
        fi
        # Backup existing .env
        cp .env .env.backup.$(date +%Y%m%d_%H%M%S)
        echo "Existing .env backed up."
    fi
    
    # Copy from example
    cp .env.example .env
    
    echo ""
    echo "Please provide your Confluence API credentials:"
    echo "(You can generate an API token at: https://id.atlassian.com/manage-profile/security/api-tokens)"
    echo ""
    
    # Get Atlassian site name
    read -p "Enter your Atlassian site name (e.g., 'mycompany' for mycompany.atlassian.net): " site_name
    if [ -n "$site_name" ]; then
        sed -i.bak "s/ATLASSIAN_SITE_NAME=.*/ATLASSIAN_SITE_NAME=$site_name/" .env
    fi
    
    # Get user email
    read -p "Enter your Atlassian user email: " user_email
    if [ -n "$user_email" ]; then
        sed -i.bak "s/ATLASSIAN_USER_EMAIL=.*/ATLASSIAN_USER_EMAIL=$user_email/" .env
    fi
    
    # Get API token (hidden input)
    echo -n "Enter your Atlassian API token: "
    read -s api_token
    echo ""
    if [ -n "$api_token" ]; then
        sed -i.bak "s/ATLASSIAN_API_TOKEN=.*/ATLASSIAN_API_TOKEN=$api_token/" .env
    fi
    
    # Clean up backup files
    rm -f .env.bak
    
    echo -e "${GREEN}✓ Environment configuration complete${NC}"
    echo ""
}

# Select deployment mode
select_mode() {
    echo -e "${BLUE}Deployment Mode${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "1) Development mode (with debugging and hot reload)"
    echo "2) Production mode (optimized and secure)"
    echo ""
    
    read -p "Select mode [1-2]: " mode_choice
    
    case $mode_choice in
        1)
            MODE="dev"
            echo -e "${GREEN}✓ Development mode selected${NC}"
            ;;
        2)
            MODE="prod"
            echo -e "${GREEN}✓ Production mode selected${NC}"
            ;;
        *)
            echo -e "${YELLOW}Invalid choice. Using development mode.${NC}"
            MODE="dev"
            ;;
    esac
    
    echo ""
}

# Build Docker image
build_image() {
    echo -e "${BLUE}Building Docker Image${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    ./docker/build.sh $MODE
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Build failed. Please check the error messages above.${NC}"
        exit 1
    fi
    
    echo ""
}

# Start containers
start_containers() {
    echo -e "${BLUE}Starting Containers${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    ./docker/run.sh $MODE --detach
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to start containers. Please check the error messages above.${NC}"
        exit 1
    fi
    
    echo ""
}

# Show final instructions
show_summary() {
    echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║           Confluence MCP Server is now running!            ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    echo -e "${CYAN}Access Points:${NC}"
    echo "  • Application: http://localhost:8081"
    echo "  • Health Check: http://localhost:8081/actuator/health"
    echo "  • MCP Endpoint: http://localhost:8081/mcp"
    
    if [ "$MODE" = "dev" ]; then
        echo "  • Debug Port: localhost:5005"
    fi
    
    echo ""
    echo -e "${CYAN}Management Commands:${NC}"
    echo "  • View logs:        ./docker/logs.sh --follow"
    echo "  • Stop server:      ./docker/stop.sh"
    echo "  • Restart server:   ./docker/run.sh $MODE"
    
    if [ "$MODE" = "dev" ]; then
        echo "  • Debug server:     ./docker/debug.sh"
    fi
    
    echo ""
    echo -e "${CYAN}MCP Client Configuration:${NC}"
    echo "  Add to your MCP client configuration:"
    echo "  {"
    echo "    \"mcpServers\": {"
    echo "      \"confluence\": {"
    echo "        \"url\": \"http://localhost:8081/mcp\""
    echo "      }"
    echo "    }"
    echo "  }"
    
    echo ""
    echo -e "${CYAN}Documentation:${NC}"
    echo "  • MCP Client Guide: ./docs/MCP-CLIENT.md"
    echo "  • Project README: ./README.md"
    
    echo ""
    echo -e "${GREEN}Setup complete! Your Confluence MCP Server is ready to use.${NC}"
}

# Cleanup on error
cleanup_on_error() {
    echo ""
    echo -e "${RED}Setup interrupted or failed.${NC}"
    echo "Run './docker/stop.sh --clean' to clean up any partial installation."
    exit 1
}

# Main execution
main() {
    clear
    show_banner
    
    # Set up error handling
    trap cleanup_on_error ERR INT TERM
    
    # Run setup steps
    check_prerequisites
    setup_environment
    select_mode
    build_image
    start_containers
    
    # Show summary
    show_summary
    
    # Offer to show logs
    echo ""
    read -p "Would you like to view the application logs? (y/N): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        ./docker/logs.sh --follow
    fi
}

# Run main function
main