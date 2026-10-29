package com.architek.oikos.testsupport;

import java.nio.charset.StandardCharsets;

import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.architek.oikos.shared.application.port.out.EmailTemplateRendererPort;
import com.architek.oikos.shared.infrastructure.email.ThymeleafEmailTemplateRenderer;

/**
 * The real email templates, rendered outside any Spring context - for the
 * service tests that construct an email composer by hand. SpringTemplateEngine
 * rather than the bare TemplateEngine, as in production (see
 * ThymeleafPaymentReceiptRendererTest).
 */
public final class TestEmailTemplates {

    private TestEmailTemplates() {
    }

    public static EmailTemplateRendererPort renderer() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return new ThymeleafEmailTemplateRenderer(engine, "Daba Syndic", "http://localhost:5173");
    }
}
