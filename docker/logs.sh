#!/usr/bin/env bash

# Logs script for Confluence MCP Server Docker container
# Usage: ./docker/logs.sh [--follow] [--tail N] [--since TIME]

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
FOLLOW=""
TAIL_LINES="100"
SINCE=""
CONTAINER_NAME="confluence-mcp-server"
LOG_LEVEL_FILTER=""
TIMESTAMPS="--timestamps"

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --follow|-f)
            FOLLOW="--follow"
            shift
            ;;
        --tail|-t)
            TAIL_LINES="$2"
            shift 2
            ;;
        --since|-s)
            SINCE="--since $2"
            shift 2
            ;;
        --level)
            LOG_LEVEL_FILTER="$2"
            shift 2
            ;;
        --no-timestamps)
            TIMESTAMPS=""
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  -f, --follow         Follow log output"
            echo "  -t, --tail N         Number of lines to show from the end (default: 100)"
            echo "  -s, --since TIME     Show logs since timestamp (e.g., 2013-01-02T13:23:37)"
            echo "                       or relative (e.g., 10m for 10 minutes ago)"
            echo "  --level LEVEL        Filter by log level (ERROR, WARN, INFO, DEBUG)"
            echo "  --no-timestamps      Don't show timestamps"
            echo "  -h, --help          Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0 --follow                    # Follow live logs"
            echo "  $0 --tail 50                   # Show last 50 lines"
            echo "  $0 --since 10m                  # Show logs from last 10 minutes"
            echo "  $0 --level ERROR               # Show only ERROR logs"
            echo "  $0 --follow --level DEBUG      # Follow DEBUG and higher logs"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Function to check if container exists
check_container() {
    if ! docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        echo -e "${RED}Error: Container '${CONTAINER_NAME}' not found${NC}"
        echo ""
        echo "Available containers:"
        docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Image}}"
        exit 1
    fi
    
    # Check if container is running
    if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        echo -e "${YELLOW}Warning: Container '${CONTAINER_NAME}' is not running${NC}"
        echo "Status: $(docker ps -a --filter "name=${CONTAINER_NAME}" --format "{{.Status}}")"
        echo ""
    fi
}

# Function to build docker logs command
build_logs_command() {
    local cmd="docker logs"
    
    [ -n "$TIMESTAMPS" ] && cmd="$cmd $TIMESTAMPS"
    [ -n "$FOLLOW" ] && cmd="$cmd $FOLLOW"
    [ -n "$SINCE" ] && cmd="$cmd $SINCE"
    
    cmd="$cmd --tail $TAIL_LINES $CONTAINER_NAME"
    
    echo "$cmd"
}

# Function to apply log level filter
apply_log_filter() {
    if [ -n "$LOG_LEVEL_FILTER" ]; then
        case $LOG_LEVEL_FILTER in
            ERROR)
                grep -E "ERROR|FATAL"
                ;;
            WARN)
                grep -E "ERROR|FATAL|WARN"
                ;;
            INFO)
                grep -E "ERROR|FATAL|WARN|INFO"
                ;;
            DEBUG)
                grep -E "ERROR|FATAL|WARN|INFO|DEBUG"
                ;;
            *)
                cat
                ;;
        esac
    else
        cat
    fi
}

# Function to format and colorize logs
colorize_logs() {
    # Add colors to log levels
    sed -E "s/(ERROR|FATAL)/$(printf '\033[0;31m')\1$(printf '\033[0m')/g" | \
    sed -E "s/(WARN|WARNING)/$(printf '\033[1;33m')\1$(printf '\033[0m')/g" | \
    sed -E "s/(INFO)/$(printf '\033[0;32m')\1$(printf '\033[0m')/g" | \
    sed -E "s/(DEBUG|TRACE)/$(printf '\033[0;34m')\1$(printf '\033[0m')/g"
}

# Function to show log statistics
show_log_stats() {
    echo -e "${GREEN}Log Statistics (last $TAIL_LINES lines):${NC}"
    
    # Get logs without following
    local logs=$(docker logs --tail $TAIL_LINES $CONTAINER_NAME 2>&1)
    
    # Count log levels
    local error_count=$(echo "$logs" | grep -c "ERROR" || echo "0")
    local warn_count=$(echo "$logs" | grep -c "WARN" || echo "0")
    local info_count=$(echo "$logs" | grep -c "INFO" || echo "0")
    local debug_count=$(echo "$logs" | grep -c "DEBUG" || echo "0")
    
    echo "  ERROR: $error_count"
    echo "  WARN:  $warn_count"
    echo "  INFO:  $info_count"
    echo "  DEBUG: $debug_count"
    echo ""
    
    # Show any recent errors
    if [ "$error_count" -gt 0 ]; then
        echo -e "${RED}Recent errors found in logs!${NC}"
        echo ""
    fi
}

# Main execution
main() {
    echo -e "${GREEN}Confluence MCP Server - Log Viewer${NC}"
    echo ""
    
    # Check if container exists
    check_container
    
    # Show log statistics if not following
    if [ -z "$FOLLOW" ]; then
        show_log_stats
    fi
    
    # Build and execute logs command
    LOG_CMD=$(build_logs_command)
    
    echo -e "${GREEN}Showing logs${NC}"
    if [ -n "$LOG_LEVEL_FILTER" ]; then
        echo "Filtering for level: $LOG_LEVEL_FILTER and above"
    fi
    if [ -n "$FOLLOW" ]; then
        echo "Following logs (Ctrl+C to stop)..."
    fi
    echo "----------------------------------------"
    
    # Execute the logs command with filtering and colorization
    if [ -n "$LOG_LEVEL_FILTER" ] || [ -t 1 ]; then
        # If filtering or output is a terminal, apply filters and colors
        eval "$LOG_CMD 2>&1" | apply_log_filter | colorize_logs
    else
        # Otherwise, just output raw logs
        eval "$LOG_CMD 2>&1"
    fi
}

# Handle script interruption
trap 'echo -e "\n${YELLOW}Log viewing stopped${NC}"; exit 0' INT TERM

# Run main function
main