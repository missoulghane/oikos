package com.architek.oikos.shared.infrastructure.email;

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import com.architek.oikos.shared.application.port.out.EmailTemplateRendererPort;

/**
 * Renders templates/email/{name}.html with Thymeleaf. Every one of them is
 * wrapped by templates/email/layout.html - header, footer, colours - so the
 * look of all emails changes in that one file.
 *
 * <p>Adds the variables every layout reads (brand name, public URL), so the
 * callers pass only what is specific to their email.
 */
@Component
public class ThymeleafEmailTemplateRenderer implements EmailTemplateRendererPort {

    private static final String TEMPLATE_PREFIX = "email/";
    private static final Locale FR = Locale.FRANCE;

    private final ITemplateEngine templateEngine;
    private final String brandName;
    private final String publicBaseUrl;

    public ThymeleafEmailTemplateRenderer(ITemplateEngine templateEngine,
                                          @Value("${oikos.mail.brand-name:Daba Syndic}") String brandName,
                                          @Value("${oikos.mail.public-base-url}") String publicBaseUrl) {
        this.templateEngine = templateEngine;
        this.brandName = brandName;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    public String render(String template, Map<String, Object> variables) {
        Context context = new Context(FR);
        context.setVariable("brandName", brandName);
        context.setVariable("publicBaseUrl", publicBaseUrl);
        context.setVariables(variables);
        return templateEngine.process(TEMPLATE_PREFIX + template, context);
    }
}
