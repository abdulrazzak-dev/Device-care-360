package com.devicecare360.aitroubleshooting;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class AiTroubleshootingApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiTroubleshootingApplication.class, args);
    }

    @EventListener
    public void onWebServerReady(WebServerInitializedEvent event) {
        log.info("Embedded web server initialized successfully on port: {} with namespace: {}",
                event.getWebServer().getPort(), event.getServerNamespace());
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        log.info("AiTroubleshootingApplication successfully started! Active profiles: {}, Server configured port: {}",
                Arrays.toString(env.getActiveProfiles()), env.getProperty("server.port"));
    }
}
