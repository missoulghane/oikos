package com.architek.oikos.shared.application.port.out;

import java.util.Map;

/**
 * Turns a named email template and its variables into the HTML body handed to
 * {@link EmailSenderPort}. The wording and the look of every email live in
 * those templates (resources/templates/email/), not in Java: changing a
 * sentence or the layout is an edit to an HTML file, with no code to touch.
 */
public interface EmailTemplateRendererPort {

    /**
     * @param template  the template name under templates/email/, without extension (e.g. "account-verification")
     * @param variables what the template reads; values are HTML-escaped on output
     */
    String render(String template, Map<String, Object> variables);
}
