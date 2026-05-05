package com.spendsmart.gateway.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.LinkedHashSet;
import java.util.Set;


@Configuration
public class SwaggerConfig {

    @Bean
    @Primary
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();

        Set<SwaggerUrl> urls = new LinkedHashSet<>();

        // Auth Service — port 8081
        urls.add(createSwaggerUrl("Auth Service",             "/v3/api-docs/auth-service"));

        // Category Service — port 8082
        urls.add(createSwaggerUrl("Category Service",         "/v3/api-docs/category-service"));

        // Expense Service — port 8083
        urls.add(createSwaggerUrl("Expense Service",          "/v3/api-docs/expense-service"));

        // Income Service — port 8084
        urls.add(createSwaggerUrl("Income Service",           "/v3/api-docs/income-service"));

        // Budget & Recurring Service — port 8085
        urls.add(createSwaggerUrl("Budget Service",           "/v3/api-docs/budget-service"));

        // Analytics Service — port 8086
        urls.add(createSwaggerUrl("Analytics Service",        "/v3/api-docs/analytics-service"));

        properties.setUrls(urls);

        // Show Auth Service first by default when Swagger UI opens
        properties.setUrlsPrimaryName("Auth Service");

        return properties;
    }

    private SwaggerUrl createSwaggerUrl(String name, String url) {
        SwaggerUrl swaggerUrl = new SwaggerUrl();
        swaggerUrl.setName(name);
        swaggerUrl.setUrl(url);
        return swaggerUrl;
    }
}