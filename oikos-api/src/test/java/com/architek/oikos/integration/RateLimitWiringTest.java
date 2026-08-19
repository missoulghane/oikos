package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import com.architek.oikos.shared.infrastructure.ratelimit.RateLimitFilter;

/**
 * Le contexte démarre-t-il vraiment avec l'anti-abus activé ? La suite le laisse
 * désactivé (les plafonds seraient partagés entre les cas d'un même contexte), ce
 * qui veut dire que sans ce test, l'enregistrement du filtre n'est instancié
 * nulle part avant la production.
 *
 * <p>C'est exactement ce qui est arrivé : le filtre demandait un ObjectMapper
 * {@code com.fasterxml}, alors que Spring Boot 4 auto-configure celui de Jackson
 * 3 ({@code tools.jackson}) - les 1219 tests passaient, et l'application refusait
 * de démarrer. Même leçon que WhatsAppAdapterWiringTest, à côté.
 */
class RateLimitWiringTest {

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = "oikos.security.rate-limit.enabled=true")
    class WhenTheAntiAbuseIsOn {

        @Autowired
        private FilterRegistrationBean<RateLimitFilter> registration;

        @Test
        void the_filter_is_registered_ahead_of_the_security_chain() {
            assertThat(registration.getFilter()).isInstanceOf(RateLimitFilter.class);
            // La chaîne Spring Security s'enregistre à -100 : le filtre doit
            // passer avant, sinon une tentative de mot de passe serait comptée
            // après avoir été authentifiée.
            assertThat(registration.getOrder()).isLessThan(-100);
        }
    }

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = "oikos.security.rate-limit.enabled=false")
    class WhenTheAntiAbuseIsOff {

        @Autowired
        private ApplicationContext context;

        @Test
        void no_filter_is_registered_at_all() {
            assertThat(context.getBeanNamesForType(FilterRegistrationBean.class))
                    .doesNotContain("rateLimitFilterRegistration");
        }
    }
}
