package io.github.greenstevester.confluencemcpsvr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Configures dynamic port assignment when the default port is in use
 */
@Component
public class PortManagementConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    
    private static final Logger logger = LoggerFactory.getLogger(PortManagementConfiguration.class);
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";
    
    private final Environment environment;
    
    public PortManagementConfiguration(Environment environment) {
        this.environment = environment;
    }
    
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        int configuredPort = environment.getProperty("server.port", Integer.class, 8081);
        
        // Check if the configured port is available
        if (isPortAvailable(configuredPort)) {
            logger.info("Port {} is available, using configured port", configuredPort);
            factory.setPort(configuredPort);
        } else {
            // Port is in use, fall back to dynamic assignment
            logger.warn("Port {} is already in use, switching to dynamic port assignment", configuredPort);
            
            System.out.println();
            System.out.println(ANSI_YELLOW + "⚠️  Port " + configuredPort + " is already in use!" + ANSI_RESET);
            System.out.println(ANSI_GREEN + "🔄 Switching to dynamic port assignment..." + ANSI_RESET);
            System.out.println();
            
            factory.setPort(0); // 0 means dynamic port assignment
        }
    }
    
    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}