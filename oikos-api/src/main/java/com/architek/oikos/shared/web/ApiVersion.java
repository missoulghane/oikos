package com.architek.oikos.shared.web;

/**
 * Centralized API version prefix, applied globally by shared.infrastructure.configuration.WebMvcConfiguration.
 * Controllers stay ignorant of this prefix in their @RequestMapping.
 */
public final class ApiVersion {

    public static final String V1 = "/api/v1";

    private ApiVersion() {
    }
}
