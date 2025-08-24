package io.github.greenstevester.confluencemcpsvr.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background service that periodically prints system and MCP endpoint statistics
 */
@Service
public class BackgroundMonitoringService implements ApplicationListener<ApplicationReadyEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(BackgroundMonitoringService.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // ANSI color codes
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_MAGENTA = "\u001B[35m";
    private static final String ANSI_RESET = "\u001B[0m";
    
    @Autowired
    private SystemMetricsCollector systemMetricsCollector;
    
    @Autowired
    private McpEndpointTracker endpointTracker;
    
    @Autowired
    private Environment environment;
    
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
        r -> {
            Thread t = new Thread(r, "mcp-monitor");
            t.setDaemon(true);
            return t;
        }
    );
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Check if monitoring is enabled (can be disabled in production)
        boolean monitoringEnabled = environment.getProperty("mcp.monitoring.enabled", Boolean.class, true);
        
        if (!monitoringEnabled) {
            logger.info("Background monitoring disabled via configuration");
            return;
        }
        
        int intervalSeconds = environment.getProperty("mcp.monitoring.interval", Integer.class, 15);
        
        logger.info("Starting background monitoring service (interval: {}s)", intervalSeconds);
        
        // Start monitoring after a 10-second delay to let the application fully initialize
        scheduler.scheduleAtFixedRate(this::printMonitoringReport, 10, intervalSeconds, TimeUnit.SECONDS);
        
        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down background monitoring service");
            scheduler.shutdown();
        }));
    }
    
    private void printMonitoringReport() {
        try {
            SystemMetricsCollector.SystemMetrics metrics = systemMetricsCollector.collectMetrics();
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            
            System.out.println();
            System.out.println(ANSI_BOLD + ANSI_CYAN + "╔═══════════════════════════════════════════════════════════════╗" + ANSI_RESET);
            System.out.println(ANSI_BOLD + ANSI_CYAN + "║" + ANSI_RESET + 
                             ANSI_BOLD + "           MCP Server Monitoring Report - " + timestamp + "           " + ANSI_RESET +
                             ANSI_BOLD + ANSI_CYAN + "║" + ANSI_RESET);
            System.out.println(ANSI_BOLD + ANSI_CYAN + "╚═══════════════════════════════════════════════════════════════╝" + ANSI_RESET);
            System.out.println();
            
            // Memory Section
            System.out.println(ANSI_BOLD + ANSI_GREEN + "💾 Memory Usage" + ANSI_RESET);
            System.out.println("   ├─ Heap: " + ANSI_YELLOW + metrics.heapUsedMB + "MB" + ANSI_RESET + 
                             " / " + ANSI_YELLOW + metrics.heapMaxMB + "MB" + ANSI_RESET + 
                             " (" + formatPercentage(metrics.heapUsagePercent) + ")");
            System.out.println("   ├─ Non-Heap: " + ANSI_YELLOW + metrics.nonHeapUsedMB + "MB" + ANSI_RESET);
            System.out.println("   └─ System: " + ANSI_YELLOW + 
                             (metrics.totalSystemMemoryMB - metrics.freeSystemMemoryMB) + "MB" + ANSI_RESET +
                             " / " + ANSI_YELLOW + metrics.totalSystemMemoryMB + "MB" + ANSI_RESET +
                             " (Free: " + ANSI_GREEN + metrics.freeSystemMemoryMB + "MB" + ANSI_RESET + ")");
            System.out.println();
            
            // CPU Section
            System.out.println(ANSI_BOLD + ANSI_BLUE + "⚡ CPU Usage" + ANSI_RESET);
            System.out.println("   ├─ System: " + formatPercentage(metrics.systemCpuUsage));
            System.out.println("   └─ Process: " + formatPercentage(metrics.processCpuUsage));
            System.out.println();
            
            // MCP Endpoints Section
            String endpointStats = endpointTracker.getFormattedStats();
            System.out.println(endpointStats);
            System.out.println();
            
        } catch (Exception e) {
            logger.warn("Error generating monitoring report: {}", e.getMessage());
        }
    }
    
    private String formatPercentage(double percentage) {
        if (Double.isNaN(percentage) || percentage < 0) {
            return ANSI_MAGENTA + "N/A" + ANSI_RESET;
        }
        
        String color = ANSI_GREEN;
        if (percentage > 80) {
            color = ANSI_RED;
        } else if (percentage > 60) {
            color = ANSI_YELLOW;
        }
        
        return color + String.format("%.1f%%", percentage) + ANSI_RESET;
    }
}