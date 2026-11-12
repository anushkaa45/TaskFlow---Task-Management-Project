
package com.taskflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI taskFlowOpenAPI() {

        return new OpenAPI()

                .info(new Info()
                        .title("TaskFlow API")
                        .description(
                                "REST APIs for TaskFlow - " +
                                        "Project and Task Management System. " +
                                        "Manage users, projects, tasks, teams, " +
                                        "comments and notifications."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TaskFlow Development Team")
                        )
                )

                // Configure JWT authentication
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token")
                        )
                )

                // Apply JWT security to documented APIs
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                );
    }
}
