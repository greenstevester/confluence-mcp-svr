package io.github.greenstevester.confluencemcpsvr.monitoring;

import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import com.sun.management.OperatingSystemMXBean;

/**
 * Collects system metrics like CPU and memory usage
 */
@Component
public class SystemMetricsCollector {
    
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osBean = 
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    
    public SystemMetrics collectMetrics() {
        // Memory metrics
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
        
        long heapUsedMB = heapMemory.getUsed() / (1024 * 1024);
        long heapMaxMB = heapMemory.getMax() / (1024 * 1024);
        long nonHeapUsedMB = nonHeapMemory.getUsed() / (1024 * 1024);
        
        double heapUsagePercent = (double) heapMemory.getUsed() / heapMemory.getMax() * 100;
        
        // CPU metrics
        double cpuUsage = osBean.getCpuLoad() * 100;
        double processCpuUsage = osBean.getProcessCpuLoad() * 100;
        
        // System memory
        long totalSystemMemoryMB = osBean.getTotalMemorySize() / (1024 * 1024);
        long freeSystemMemoryMB = osBean.getFreeMemorySize() / (1024 * 1024);
        
        return new SystemMetrics(
            heapUsedMB, heapMaxMB, heapUsagePercent,
            nonHeapUsedMB, 
            cpuUsage, processCpuUsage,
            totalSystemMemoryMB, freeSystemMemoryMB
        );
    }
    
    /**
     * System metrics data class
     */
    public static class SystemMetrics {
        public final long heapUsedMB;
        public final long heapMaxMB;
        public final double heapUsagePercent;
        public final long nonHeapUsedMB;
        public final double systemCpuUsage;
        public final double processCpuUsage;
        public final long totalSystemMemoryMB;
        public final long freeSystemMemoryMB;
        
        public SystemMetrics(long heapUsedMB, long heapMaxMB, double heapUsagePercent, 
                           long nonHeapUsedMB, double systemCpuUsage, double processCpuUsage,
                           long totalSystemMemoryMB, long freeSystemMemoryMB) {
            this.heapUsedMB = heapUsedMB;
            this.heapMaxMB = heapMaxMB;
            this.heapUsagePercent = heapUsagePercent;
            this.nonHeapUsedMB = nonHeapUsedMB;
            this.systemCpuUsage = systemCpuUsage;
            this.processCpuUsage = processCpuUsage;
            this.totalSystemMemoryMB = totalSystemMemoryMB;
            this.freeSystemMemoryMB = freeSystemMemoryMB;
        }
    }
}