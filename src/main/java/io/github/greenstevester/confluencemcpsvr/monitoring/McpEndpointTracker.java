package io.github.greenstevester.confluencemcpsvr.monitoring;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks MCP endpoint usage statistics
 */
@Component
public class McpEndpointTracker {
    
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    
    private final Map<String, EndpointStats> endpointStats = new ConcurrentHashMap<>();
    private final AtomicLong totalDataTransferredBytes = new AtomicLong(0);
    private final AtomicLong totalRequests = new AtomicLong(0);
    
    /**
     * Record an endpoint hit
     */
    public void recordEndpointHit(String endpoint, long responseSize) {
        endpointStats.computeIfAbsent(endpoint, k -> new EndpointStats())
                    .recordHit(responseSize);
        totalDataTransferredBytes.addAndGet(responseSize);
        totalRequests.incrementAndGet();
    }
    
    /**
     * Get formatted endpoint statistics
     */
    public String getFormattedStats() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(ANSI_BOLD).append(ANSI_BLUE).append("📊 MCP Endpoint Statistics").append(ANSI_RESET).append("\n");
        
        if (endpointStats.isEmpty()) {
            sb.append("   └─ ").append(ANSI_CYAN).append("No endpoint hits recorded").append(ANSI_RESET);
            return sb.toString();
        }
        
        // Total summary
        double totalDataMB = totalDataTransferredBytes.get() / (1024.0 * 1024.0);
        sb.append("   ├─ Total Requests: ").append(ANSI_YELLOW).append(totalRequests.get()).append(ANSI_RESET).append("\n");
        sb.append("   ├─ Total Data: ").append(ANSI_YELLOW).append(String.format("%.2f MB", totalDataMB)).append(ANSI_RESET).append("\n");
        
        // Individual endpoint stats
        endpointStats.entrySet().stream()
            .sorted(Map.Entry.<String, EndpointStats>comparingByValue((a, b) -> 
                Long.compare(b.getHitCount(), a.getHitCount())))
            .forEach(entry -> {
                String endpoint = entry.getKey();
                EndpointStats stats = entry.getValue();
                double dataMB = stats.getTotalBytes() / (1024.0 * 1024.0);
                
                sb.append("   ├─ ").append(ANSI_GREEN).append(endpoint).append(ANSI_RESET)
                  .append(" (").append(ANSI_YELLOW).append(stats.getHitCount()).append(" hits").append(ANSI_RESET)
                  .append(", ").append(ANSI_YELLOW).append(String.format("%.2f MB", dataMB)).append(ANSI_RESET)
                  .append(")\n");
            });
        
        // Remove trailing newline and add final connector
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
            int lastIndex = sb.lastIndexOf("├─");
            if (lastIndex >= 0) {
                sb.replace(lastIndex, lastIndex + 2, "└─");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Reset statistics
     */
    public void resetStats() {
        endpointStats.clear();
        totalDataTransferredBytes.set(0);
        totalRequests.set(0);
    }
    
    /**
     * Endpoint statistics holder
     */
    private static class EndpointStats {
        private final AtomicLong hitCount = new AtomicLong(0);
        private final AtomicLong totalBytes = new AtomicLong(0);
        private volatile LocalDateTime lastHit = LocalDateTime.now();
        
        public void recordHit(long responseSize) {
            hitCount.incrementAndGet();
            totalBytes.addAndGet(responseSize);
            lastHit = LocalDateTime.now();
        }
        
        public long getHitCount() {
            return hitCount.get();
        }
        
        public long getTotalBytes() {
            return totalBytes.get();
        }
        
        public LocalDateTime getLastHit() {
            return lastHit;
        }
    }
}