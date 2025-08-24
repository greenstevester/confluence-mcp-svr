#!/bin/bash

# IntelliJ IDEA Run Configuration Generator
# This script creates an IntelliJ IDEA run configuration that loads environment variables from .env file

set -e

PROJECT_NAME="confluence-mcp-svr"
MAIN_CLASS="io.github.greenstevester.confluence_mcp_svr.ConfluenceMcpSvrApplication"
ENV_FILE=".env"
IDEA_DIR=".idea"
RUN_CONFIGS_DIR="$IDEA_DIR/runConfigurations"

echo "🚀 Creating IntelliJ IDEA run configuration for $PROJECT_NAME"

# Check if .env file exists
if [[ ! -f "$ENV_FILE" ]]; then
    echo "❌ Error: .env file not found. Please create .env file first."
    exit 1
fi

# Create .idea/runConfigurations directory if it doesn't exist
mkdir -p "$RUN_CONFIGS_DIR"

# Read environment variables from .env file and format for IntelliJ
ENV_VARS=""
while IFS= read -r line || [[ -n "$line" ]]; do
    # Skip empty lines and comments
    if [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]]; then
        continue
    fi
    
    # Extract key=value pairs
    if [[ "$line" =~ ^[[:space:]]*([^=]+)=(.*)$ ]]; then
        key="${BASH_REMATCH[1]}"
        value="${BASH_REMATCH[2]}"
        
        # Clean up key and value (remove leading/trailing whitespace)
        key=$(echo "$key" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
        value=$(echo "$value" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
        
        # Add to environment variables list
        if [[ -n "$ENV_VARS" ]]; then
            ENV_VARS="$ENV_VARS;"
        fi
        ENV_VARS="$ENV_VARS$key=$value"
    fi
done < "$ENV_FILE"

# Generate IntelliJ run configuration XML
CONFIG_FILE="$RUN_CONFIGS_DIR/ConfluenceMcpSvrApplication.xml"

cat > "$CONFIG_FILE" << EOF
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="ConfluenceMcpSvrApplication" type="Application" factoryName="Application">
    <envs>
EOF

# Add individual environment variables
while IFS= read -r line || [[ -n "$line" ]]; do
    # Skip empty lines and comments
    if [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]]; then
        continue
    fi
    
    # Extract key=value pairs
    if [[ "$line" =~ ^[[:space:]]*([^=]+)=(.*)$ ]]; then
        key="${BASH_REMATCH[1]}"
        value="${BASH_REMATCH[2]}"
        
        # Clean up key and value
        key=$(echo "$key" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
        value=$(echo "$value" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
        
        # Escape XML special characters in value
        value=$(echo "$value" | sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g; s/"/\&quot;/g; s/'"'"'/\&#39;/g')
        
        echo "      <env name=\"$key\" value=\"$value\" />" >> "$CONFIG_FILE"
    fi
done < "$ENV_FILE"

cat >> "$CONFIG_FILE" << EOF
    </envs>
    <option name="MAIN_CLASS_NAME" value="$MAIN_CLASS" />
    <module name="$PROJECT_NAME.main" />
    <option name="PROGRAM_PARAMETERS" value="" />
    <option name="VM_PARAMETERS" value="" />
    <option name="WORKING_DIRECTORY" value="\$PROJECT_DIR\$" />
    <method v="2">
      <option name="Make" enabled="true" />
    </method>
  </configuration>
</component>
EOF

echo "✅ IntelliJ IDEA run configuration created: $CONFIG_FILE"
echo ""
echo "📋 Environment variables loaded from .env:"
while IFS= read -r line || [[ -n "$line" ]]; do
    if [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]]; then
        continue
    fi
    if [[ "$line" =~ ^[[:space:]]*([^=]+)=(.*)$ ]]; then
        key="${BASH_REMATCH[1]}"
        key=$(echo "$key" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
        echo "   - $key"
    fi
done < "$ENV_FILE"

echo ""
echo "🔄 Next steps:"
echo "1. Restart IntelliJ IDEA or refresh the project"
echo "2. Go to Run -> Edit Configurations"
echo "3. You should see 'ConfluenceMcpSvrApplication' configuration"
echo "4. Run the application with the loaded environment variables"
echo ""
echo "💡 Tip: If you update .env file, re-run this script to update the configuration"