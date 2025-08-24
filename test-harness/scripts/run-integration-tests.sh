#!/usr/bin/env bash

# MCP Integration Tests Runner
# Builds the server and runs comprehensive MCP integration tests

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
echo -e "${BLUE}║  MCP Integration Tests Runner          ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo ""

cd "$PROJECT_ROOT"

# Check if .env file exists
if [ ! -f .env ]; then
    echo -e "${YELLOW}Warning: .env file not found${NC}"
    echo "Integration tests will run with default/mock configuration"
    echo "For full testing with real Confluence instance, create .env with your credentials"
    echo ""
fi

# Parse command line arguments
SKIP_BUILD=false
VERBOSE=false
TEST_PATTERN="*MCP*"

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --verbose|-v)
            VERBOSE=true
            shift
            ;;
        --pattern|-p)
            TEST_PATTERN="$2"
            shift 2
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --skip-build    Skip building the JAR file"
            echo "  --verbose, -v   Enable verbose test output"
            echo "  --pattern, -p   Test pattern to run (default: *MCP*)"
            echo "  --help, -h      Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                           # Run all MCP tests"
            echo "  $0 --skip-build              # Run tests without rebuilding"
            echo "  $0 -p \"*Confluence*\"        # Run only Confluence tests"
            echo "  $0 --verbose                 # Run with verbose output"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Build the server JAR if not skipped
if [ "$SKIP_BUILD" = false ]; then
    echo -e "${GREEN}Building the server...${NC}"
    ./gradlew build -x test || {
        echo -e "${RED}Build failed. Please fix build errors and try again.${NC}"
        exit 1
    }
    echo -e "${GREEN}✓ Build completed successfully${NC}"
    echo ""
else
    echo -e "${YELLOW}Skipping build (--skip-build specified)${NC}"
    echo ""
fi

# Verify JAR file exists
JAR_FILE=$(find build/libs -name "*.jar" | grep -v plain | head -n1)
if [ -z "$JAR_FILE" ] || [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}Error: Server JAR file not found${NC}"
    echo "Please run './gradlew build' first or remove --skip-build flag"
    exit 1
fi

echo -e "${GREEN}Using server JAR: $JAR_FILE${NC}"
echo ""

# Set up test environment variables
export SPRING_PROFILES_ACTIVE=test

# Prepare test arguments
TEST_ARGS=(
    "test"
    "--tests" "*${TEST_PATTERN}*"
    "--info"
)

if [ "$VERBOSE" = true ]; then
    TEST_ARGS+=(
        "--debug"
        "-Dorg.gradle.logging.level=debug"
    )
fi

# Run the integration tests
echo -e "${GREEN}Running MCP integration tests...${NC}"
echo -e "${BLUE}Test pattern: ${TEST_PATTERN}${NC}"
echo -e "${BLUE}Test profile: test${NC}"
echo ""

# Add system properties for MCP tests
export MCP_SERVER_JAR_PATH="$JAR_FILE"

# Run tests with proper configuration
./gradlew "${TEST_ARGS[@]}" \
    -Dtest.mcp.jar.path="$JAR_FILE" \
    -Dspring.profiles.active=test \
    -Djunit.jupiter.execution.parallel.enabled=false

TEST_EXIT_CODE=$?

echo ""
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  All MCP integration tests passed!     ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${GREEN}✓ MCP protocol compatibility verified${NC}"
    echo -e "${GREEN}✓ Tool functionality validated${NC}"
    echo -e "${GREEN}✓ Error handling confirmed${NC}"
    echo -e "${GREEN}✓ Server capabilities tested${NC}"
else
    echo -e "${RED}╔════════════════════════════════════════╗${NC}"
    echo -e "${RED}║  Some MCP integration tests failed     ║${NC}"
    echo -e "${RED}╚════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${YELLOW}Check the test output above for details${NC}"
    echo -e "${YELLOW}Test report: build/reports/tests/test/index.html${NC}"
fi

echo ""
echo -e "${CYAN}Additional testing options:${NC}"
echo "• Manual testing: ./test-harness/scripts/test-with-inspector.sh"
echo "• Performance tests: ./gradlew test --tests \"*Performance*\""
echo "• Specific tool tests: ./gradlew test --tests \"*ConfluenceSpaces*\""

exit $TEST_EXIT_CODE