package com.architek.oikos.shared.infrastructure.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.architek.oikos.shared.domain.service.RateLimiter;

import tools.jackson.databind.ObjectMapper;

class RateLimitFilterTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-08-19T10:00:00Z"), ZoneOffset.UTC);

    private static final RateLimitPolicies POLICIES = new RateLimitPolicies(
            true, Duration.ofMinutes(5), 3, 2, 2, 2, 3);

    private RateLimitFilter newFilter() {
        return new RateLimitFilter(new RateLimiter(FIXED_CLOCK), POLICIES, new ObjectMapper());
    }

    private static MockHttpServletRequest post(String path, String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr(ip);
        return request;
    }

    private static MockHttpServletResponse pass(RateLimitFilter filter, MockHttpServletRequest request)
            throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void refuses_a_fourth_login_attempt_from_the_same_address() throws Exception {
        RateLimitFilter filter = newFilter();
        for (int attempt = 0; attempt < 3; attempt++) {
            assertThat(pass(filter, post("/api/v1/auth/login", "10.0.0.1")).getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        MockHttpServletResponse refused = pass(filter, post("/api/v1/auth/login", "10.0.0.1"));

        assertThat(refused.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(refused.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("300");
        assertThat(refused.getContentAsString()).contains("Trop de tentatives");
    }

    @Test
    void the_body_is_the_error_shape_the_rest_of_the_api_returns() throws Exception {
        // Le filtre écrit hors du MVC : sans ce format commun, un client devrait
        // savoir quel étage l'a refusé pour lire le message.
        RateLimitFilter filter = newFilter();
        for (int attempt = 0; attempt < 3; attempt++) {
            pass(filter, post("/api/v1/auth/login", "10.0.0.1"));
        }

        String body = pass(filter, post("/api/v1/auth/login", "10.0.0.1")).getContentAsString();

        assertThat(body).contains("\"status\":429")
                .contains("\"error\":\"Too Many Requests\"")
                .contains("\"path\":\"/api/v1/auth/login\"")
                .contains("\"timestamp\"");
    }

    @Test
    void one_address_being_blocked_leaves_the_others_alone() throws Exception {
        RateLimitFilter filter = newFilter();
        for (int attempt = 0; attempt < 4; attempt++) {
            pass(filter, post("/api/v1/auth/login", "10.0.0.1"));
        }

        assertThat(pass(filter, post("/api/v1/auth/login", "10.0.0.2")).getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void each_endpoint_counts_separately() throws Exception {
        // Sinon, un visiteur qui vient d'épuiser ses tentatives de connexion ne
        // pourrait plus demander la réinitialisation qui l'en sortirait.
        RateLimitFilter filter = newFilter();
        for (int attempt = 0; attempt < 4; attempt++) {
            pass(filter, post("/api/v1/auth/login", "10.0.0.1"));
        }

        assertThat(pass(filter, post("/api/v1/auth/forgot-password", "10.0.0.1")).getStatus())
                .isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void endpoints_that_are_not_capped_are_not_counted_either() throws Exception {
        RateLimitFilter filter = newFilter();

        for (int attempt = 0; attempt < 20; attempt++) {
            assertThat(pass(filter, post("/api/v1/auth/refresh-token", "10.0.0.1")).getStatus())
                    .isEqualTo(HttpStatus.OK.value());
        }
    }

    @Test
    void a_GET_on_a_capped_path_is_left_alone() throws Exception {
        // Les plafonds visent des écritures ; compter les préflights et les
        // lectures ferait tomber des requêtes qui ne coûtent rien.
        RateLimitFilter filter = newFilter();
        MockHttpServletRequest read = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        read.setRemoteAddr("10.0.0.1");

        for (int attempt = 0; attempt < 10; attempt++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(read, response, new MockFilterChain());
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }
    }
}
