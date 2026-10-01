package com.minisearchengine.rest;

import com.minisearchengine.rest.config.WebConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Boots the full Spring context (mock web environment) to verify that
 * MiniSearchApplication + WebConfig + application.properties wire up
 * correctly. This is a wiring smoke test only — engine logic is covered
 * by the pure-Java test suites.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class MiniSearchApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void contextLoads() {
        assertNotNull(context);
        assertNotNull(context.getBean(WebConfig.class));
        assertEquals("8080",
                context.getEnvironment().getProperty("server.port"),
                "backend must run on port 8080 (UI dev server uses 8443)");
    }
}
