package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Every link a copropriétaire receives derives from one variable, and this is
 * what keeps that true.
 *
 * <p>It exists because of a real incident: the convocation's confirmation link -
 * printed in the email, in the letter and inside the QR code - pointed at
 * localhost in the staging deployment. Not because anything was misconfigured
 * there, but because it was one more independent variable nobody knew to set,
 * and its default was silently wrong. Nothing failed at startup, no test caught
 * it, and it surfaced only when someone read a letter.
 *
 * <p>So the assertions below are not about YAML syntax. They pin the property
 * that made the incident possible in the first place - a link URL that is never
 * left to its own default - and the escape hatch that keeps a one-off host from
 * forcing everything back to six variables.
 */
class PublicBaseUrlConfigurationTest {

    /** The six links, as a deployment that sets only the public origin gets them. */
    @Nested
    @SpringBootTest
    @TestPropertySource(properties = "APP_PUBLIC_BASE_URL=https://oikos-staging.tech")
    class WhenOnlyThePublicOriginIsSet {

        @Value("${oikos.mail.convocation-confirmation-base-url}")
        private String convocationConfirmationBaseUrl;

        @Value("${oikos.mail.verification-base-url}")
        private String verificationBaseUrl;

        @Value("${oikos.mail.account-activation-base-url}")
        private String accountActivationBaseUrl;

        @Value("${oikos.mail.password-reset-base-url}")
        private String passwordResetBaseUrl;

        @Value("${oikos.mail.party-invitation-base-url}")
        private String partyInvitationBaseUrl;

        @Value("${oikos.mail.invitation-base-url}")
        private String invitationBaseUrl;

        @Test
        void every_public_link_follows_it() {
            assertThat(convocationConfirmationBaseUrl)
                    .isEqualTo("https://oikos-staging.tech/convocations/confirmation");
            // Le seul lien qui pointe vers l'API et non vers oikos-web : il est surchargé
            // dans la configuration de test, ce qui vaut aussi démonstration que la surcharge
            // individuelle survit à la dérivation.
            assertThat(verificationBaseUrl).isEqualTo("http://localhost:8080/api/v1/users/verify");
            assertThat(accountActivationBaseUrl).isEqualTo("https://oikos-staging.tech/activate-account");
            assertThat(passwordResetBaseUrl).isEqualTo("https://oikos-staging.tech/reset-password");
            assertThat(partyInvitationBaseUrl).isEqualTo("https://oikos-staging.tech/accept-invitation");
            assertThat(invitationBaseUrl).isEqualTo("https://oikos-staging.tech/invitations");
        }

        @Test
        void none_of_them_can_still_be_pointing_at_a_developer_machine() {
            assertThat(convocationConfirmationBaseUrl).doesNotContain("localhost");
            assertThat(accountActivationBaseUrl).doesNotContain("localhost");
            assertThat(passwordResetBaseUrl).doesNotContain("localhost");
            assertThat(partyInvitationBaseUrl).doesNotContain("localhost");
            assertThat(invitationBaseUrl).doesNotContain("localhost");
        }
    }

    /** One link hosted elsewhere must stay possible - it is simply no longer the norm. */
    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {
            "APP_PUBLIC_BASE_URL=https://oikos-staging.tech",
            "APP_CONVOCATION_CONFIRMATION_BASE_URL=https://ag.example.org/confirmer" })
    class WhenOneLinkIsOverriddenOnItsOwn {

        @Value("${oikos.mail.convocation-confirmation-base-url}")
        private String convocationConfirmationBaseUrl;

        @Value("${oikos.mail.invitation-base-url}")
        private String invitationBaseUrl;

        @Test
        void the_override_wins_and_the_others_are_untouched() {
            assertThat(convocationConfirmationBaseUrl).isEqualTo("https://ag.example.org/confirmer");
            assertThat(invitationBaseUrl).isEqualTo("https://oikos-staging.tech/invitations");
        }
    }
}
