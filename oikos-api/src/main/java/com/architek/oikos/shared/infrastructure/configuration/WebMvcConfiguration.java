package com.architek.oikos.shared.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.architek.oikos.shared.web.ApiVersion;

/**
 * Applies the centralized /api/v1 prefix to every @RestController so that
 * individual controllers stay ignorant of API versioning (rule 8). Scoped to our
 * own base package: HandlerTypePredicate.forAnnotation(RestController.class) alone
 * would also catch third-party @RestController beans (e.g. springdoc's OpenAPI
 * resource), silently moving /v3/api-docs to /api/v1/v3/api-docs and breaking
 * Swagger UI.
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private static final String BASE_PACKAGE = "com.architek.oikos";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(ApiVersion.V1,
                HandlerTypePredicate.forAnnotation(RestController.class).and(HandlerTypePredicate.forBasePackage(BASE_PACKAGE)));
    }
}
