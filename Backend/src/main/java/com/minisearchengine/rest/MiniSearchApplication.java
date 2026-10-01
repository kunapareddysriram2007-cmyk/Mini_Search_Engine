package com.minisearchengine.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * Spring Boot entry point for the Mini Search Engine REST API.
 *
 * Layering rule (BACKEND_PLAN.md §5):
 *   - The search ENGINE (packages {@code com.minisearchengine.core.*},
 *     {@code com.minisearchengine.engine.*}) is PURE Java: no Spring imports,
 *     no java.util.*. It lives in the repository-root src/ tree and is reused
 *     verbatim by this build (see Backend/pom.xml build-helper config).
 *   - This package (com.minisearchengine.rest) is the ONLY Spring-aware layer:
 *     controllers, services, DTOs, configuration.
 *
 * Run:  mvn -f Backend/pom.xml spring-boot:run   (base URL http://localhost:8080)
 */
@SpringBootApplication
public class MiniSearchApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(MiniSearchApplication.class, args);
        var props = ctx.getEnvironment().getProperty("server.port", "8080");
        System.out.println("[MiniSearch] REST API ready on http://localhost:" + props + "/api");
    }
}
