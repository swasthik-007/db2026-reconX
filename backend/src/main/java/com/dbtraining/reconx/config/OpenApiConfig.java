package com.dbtraining.reconx.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================================
 * OpenApiConfig — TICKET-ADV058
 * ============================================================================
 * WHAT:    Customises the OpenAPI document Springdoc generates:
 *          title, version, description, contact + JWT bearer security scheme.
 *
 * HOW:     Provides one OpenAPI bean and two GroupedOpenApi beans.
 *
 * WHY:     Swagger UI becomes the API contract consumed by frontend, QA,
 *          and demo audiences. Public and admin APIs are separated.
 *
 * OBSERVE:
 *          /api/swagger-ui.html
 *          /api/v1/api-docs/public
 *          /api/v1/api-docs/admin
 * ============================================================================
 */
@Configuration
public class OpenApiConfig {


    /**
     * Main OpenAPI metadata configuration.
     */
    @Bean
    public OpenAPI reconxOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("ReconX API")
                        .version("v1")
                        .description(
                                "Enterprise Trade Reconciliation Platform (Advanced Track)"
                        )
                        .contact(new Contact()
                                .name("DB TDI Training")
                                .email("tdi@db.com")
                        )
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("bearerAuth")
                );
    }


    /**
     * Public API documentation group.
     *
     * Includes:
     *   /v1/trades/**
     *   /v1/recon/**
     */
    @Bean
    public GroupedOpenApi publicApi() {

        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch(
                        "/v1/trades/**",
                        "/v1/recon/**"
                )
                .build();
    }


    /**
     * Admin API documentation group.
     *
     * Includes:
     *   /v1/admin/**
     *   /actuator/**
     */
    @Bean
    public GroupedOpenApi adminApi() {

        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch(
                        "/v1/admin/**",
                        "/actuator/**"
                )
                .build();
    }
}