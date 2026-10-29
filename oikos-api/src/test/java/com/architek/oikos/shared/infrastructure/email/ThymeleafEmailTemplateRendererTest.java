package com.architek.oikos.shared.infrastructure.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.architek.oikos.shared.application.port.out.EmailTemplateRendererPort;
import com.architek.oikos.testsupport.TestEmailTemplates;

class ThymeleafEmailTemplateRendererTest {

    private final EmailTemplateRendererPort renderer = TestEmailTemplates.renderer();

    @ParameterizedTest
    @ValueSource(strings = {"account-verification", "password-reset", "account-activation", "party-invitation",
            "owner-invitation", "board-invitation"})
    void every_link_email_is_wrapped_in_the_common_layout_and_carries_its_link(String template) {
        String html = renderer.render(template, Map.of("link", "https://app.test/x?token=abc", "fullName", "Amina",
                "propertyName", "Résidence Al Amal", "unitLabel", "A-12", "boardRoleLabel", "Trésorier",
                "ttlDays", 30));

        assertThat(html)
                .startsWith("<!DOCTYPE html>")
                .contains("Daba Syndic")
                .contains("Merci de ne pas y répondre")
                .contains("href=\"https://app.test/x?token=abc\"");
    }

    @Test
    void a_convocation_names_the_meeting_and_offers_the_personal_link_without_the_lot_code() {
        Map<String, Object> variables = new HashMap<>(convocation());
        variables.put("reminder", false);
        variables.put("confirmationLink", "https://app.test/convocations/confirmation?token=t0k");

        String html = renderer.render("convocation", variables);

        assertThat(html).contains("Vous êtes convoqué(e)").contains("AG ordinaire 2026")
                .contains("est jointe à cet email")
                .contains("href=\"https://app.test/convocations/confirmation?token=t0k\"")
                .contains("code de votre lot");
    }

    @Test
    void a_reminder_without_link_points_to_the_owner_space_and_mentions_no_attachment() {
        Map<String, Object> variables = new HashMap<>(convocation());
        variables.put("reminder", true);
        variables.put("confirmationLink", null);

        String html = renderer.render("convocation", variables);

        assertThat(html).contains("pas encore reçu votre réponse").doesNotContain("jointe à cet email")
                .contains("depuis votre espace copropriétaire").doesNotContain("code de votre lot");
    }

    @Test
    void a_rejection_without_reason_invents_none() {
        Map<String, Object> variables = new HashMap<>(Map.of("fullName", "Amina", "unitNumber", "A-12",
                "propertyName", "Résidence Al Amal"));
        variables.put("reason", null);

        assertThat(renderer.render("membership-rejected", variables))
                .contains("n'a pas été retenue").doesNotContain("Motif indiqué");
    }

    private static Map<String, Object> convocation() {
        return Map.of("meetingTitle", "AG ordinaire 2026", "meetingType", "ordinaire",
                "propertyName", "Résidence Al Amal", "lotLabel", "Bâtiment A - 12",
                "dateLabel", "lundi 15 septembre 2026 à 18h00", "venueLabel", "12 rue des Orangers");
    }

    @Test
    void variables_are_escaped_so_a_name_cannot_inject_markup() {
        String html = renderer.render("party-invitation",
                Map.of("link", "https://app.test/x", "fullName", "<script>alert(1)</script>"));

        assertThat(html).doesNotContain("<script>").contains("&lt;script&gt;");
    }
}
