# Docker Setup for Confluence MCP Server

This document provides comprehensive instructions for running the Confluence MCP Server using Docker.

## Quick Start

For first-time setup, use the interactive quick start wizard:

```bash
./docker-quickstart.sh
```

This will guide you through:
- Prerequisites check
- Environment configuration
- Building the Docker image
- Starting the server

## Prerequisites

- Docker 20.10+ installed and running
- Docker Compose 1.29+ (or Docker Desktop with Compose V2)
- Atlassian Confluence API credentials

## Project Structure

```
confluence-mcp-svr/
├── Dockerfile                 # Development/standard Docker image
├── Dockerfile.prod           # Production-optimized Docker image  
├── docker-compose.yml        # Main compose configuration
├── docker-compose.dev.yml    # Development overrides
├── .dockerignore            # Build context exclusions
├── .env.example             # Environment template
├── docker/                  # Docker utility scripts
│   ├── build.sh            # Build Docker images
│   ├── run.sh              # Start containers
│   ├── stop.sh             # Stop and clean up
│   ├── logs.sh             # View container logs
│   └── debug.sh            # Remote debugging setup
└── docker-quickstart.sh     # Interactive setup wizard
```

## Configuration

### 1. Environment Setup

Copy `.env.example` to `.env` and configure:

```bash
cp .env.example .env
```

Edit `.env` with your Confluence API credentials:

```env
# Required Confluence API Configuration
ATLASSIAN_SITE_NAME=your-site-name
ATLASSIAN_USER_EMAIL=your-email@example.com
ATLASSIAN_API_TOKEN=your-api-token-here
```

Generate an API token at: https://id.atlassian.com/manage-profile/security/api-tokens

### 2. Spring Profiles

The application supports three profiles:

- `default`: Basic configuration
- `dev`: Development with debug logging and hot reload
- `prod`: Production with optimized settings

Set via `SPRING_PROFILES_ACTIVE` in `.env`

## Docker Images

### Development Image (Dockerfile)

- Based on Eclipse Temurin JRE Alpine
- Includes debugging tools (curl, jq, tini)
- Supports hot reload and remote debugging
- Image size: ~250MB

### Production Image (Dockerfile.prod)

- Based on Google Distroless
- Minimal attack surface
- No shell or package manager
- Image size: ~200MB

## Building Images

### Development Build

```bash
./docker/build.sh dev
```

### Production Build

```bash
./docker/build.sh prod
```

### Build Options

```bash
# Force rebuild without cache
./docker/build.sh dev --no-cache

# Build specific Dockerfile
DOCKERFILE=Dockerfile.prod ./docker/build.sh
```

## Running the Server

### Development Mode

```bash
# Interactive mode (see logs)
./docker/run.sh dev

# Detached mode (background)
./docker/run.sh dev --detach
```

### Production Mode

```bash
./docker/run.sh prod --detach
```

## Container Management

### View Logs

```bash
# Follow logs in real-time
./docker/logs.sh --follow

# Show last 50 lines
./docker/logs.sh --tail 50

# Filter by log level
./docker/logs.sh --level ERROR

# Show logs from last 10 minutes
./docker/logs.sh --since 10m
```

### Stop Containers

```bash
# Stop containers
./docker/stop.sh

# Stop and remove volumes
./docker/stop.sh --clean

# Stop, remove volumes and images
./docker/stop.sh --clean-all
```

## Debugging

### Enable Remote Debugging

```bash
# Start with debug port 5005 (default)
./docker/debug.sh

# Use custom debug port
./docker/debug.sh --port 8000

# Suspend JVM until debugger connects
./docker/debug.sh --suspend
```

### IDE Configuration

#### IntelliJ IDEA
1. Run → Edit Configurations → Add → Remote JVM Debug
2. Host: `localhost`
3. Port: `5005`
4. Module: `confluence-mcp-svr`

#### VS Code
Add to `.vscode/launch.json`:
```json
{
  "type": "java",
  "name": "Debug Confluence MCP",
  "request": "attach",
  "hostName": "localhost",
  "port": 5005
}
```

## Docker Compose Configuration

### Services

- `confluence-mcp-server`: Main application service
  - Port 8081: Application
  - Port 5005: Debug (dev only)
  - Health checks enabled
  - Auto-restart policy

### Volumes

- `confluence-mcp-logs`: Persistent application logs
- `confluence-mcp-temp`: Temporary files
- `./config`: External configuration (optional)

### Networks

- `confluence-mcp-network`: Isolated bridge network

## Health Checks

The container includes health checks:

```bash
# Check health status
curl http://localhost:8081/actuator/health

# View in Docker
docker inspect confluence-mcp-server --format='{{.State.Health.Status}}'
```

## Resource Limits

Default limits (configurable in docker-compose.yml):

- CPU: 2 cores limit, 0.5 cores reserved
- Memory: 1GB limit, 512MB reserved
- JVM: 75% of container memory

## Environment Variables

Key environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `default` |
| `SERVER_PORT` | Application port | `8081` |
| `JAVA_OPTS` | JVM options | Container optimized |
| `LOGGING_LEVEL_ROOT` | Root log level | `INFO` |

## Troubleshooting

### Container won't start

1. Check Docker daemon: `docker info`
2. Verify configuration: `docker-compose config`
3. Check logs: `./docker/logs.sh`

### Out of memory errors

Increase memory limits in `.env`:
```env
JAVA_OPTS=-Xms512m -Xmx2g
```

### Connection refused

1. Verify port mapping: `docker port confluence-mcp-server`
2. Check firewall settings
3. Ensure container is healthy: `docker ps`

### Debug port already in use

Change debug port:
```bash
./docker/debug.sh --port 8001
```

## Performance Optimization

### Build Optimization

- Multi-stage builds minimize image size
- Layer caching speeds up rebuilds
- Dependency layers cached separately

### Runtime Optimization

- JVM container awareness enabled
- G1GC garbage collector for low latency
- String deduplication enabled
- Graceful shutdown configured

### Production Best Practices

1. Use production Dockerfile: `Dockerfile.prod`
2. Set appropriate resource limits
3. Enable only necessary actuator endpoints
4. Use JSON structured logging
5. Configure external volume for logs
6. Set up monitoring and alerting

## Security Considerations

1. Never commit `.env` file with credentials
2. Run containers as non-root user
3. Use read-only root filesystem where possible
4. Limit exposed ports
5. Keep base images updated
6. Scan images for vulnerabilities:
   ```bash
   docker scan confluence-mcp-svr:latest
   ```

## Monitoring

### Metrics Endpoint

```bash
curl http://localhost:8081/actuator/metrics
```

### Prometheus Metrics

```bash
curl http://localhost:8081/actuator/prometheus
```

### Container Stats

```bash
docker stats confluence-mcp-server
```

## Backup and Recovery

### Backup Configuration

```bash
# Backup environment and logs
tar -czf backup.tar.gz .env docker/logs/
```

### Export/Import Images

```bash
# Export image
docker save confluence-mcp-svr:prod > confluence-mcp.tar

# Import image
docker load < confluence-mcp.tar
```

## CI/CD Integration

### GitHub Actions Example

```yaml
- name: Build and push Docker image
  run: |
    docker build -f Dockerfile.prod -t confluence-mcp-svr:${{ github.sha }} .
    docker tag confluence-mcp-svr:${{ github.sha }} confluence-mcp-svr:latest
```

### Jenkins Pipeline

```groovy
stage('Build Docker Image') {
    steps {
        sh './docker/build.sh prod'
    }
}
```

## Support

For issues or questions:
1. Check application logs: `./docker/logs.sh`
2. Review this documentation
3. Check Docker daemon status
4. Verify environment configuration