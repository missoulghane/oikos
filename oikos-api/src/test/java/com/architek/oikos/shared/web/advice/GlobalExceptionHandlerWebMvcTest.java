package com.architek.oikos.shared.web.advice;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies GlobalExceptionHandler maps every exception type to the standardized
 * ErrorResponse and the expected HTTP status, via a throwaway test controller.
 */
@WebMvcTest(controllers = ThrowingTestController.class)
class GlobalExceptionHandlerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void resource_not_found_maps_to_404() throws Exception {
        mockMvc.perform(get("/api/v1/test/not-found").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @WithMockUser
    void unauthorized_maps_to_403() throws Exception {
        mockMvc.perform(get("/api/v1/test/unauthorized").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @WithMockUser
    void optimistic_locking_failure_maps_to_409() throws Exception {
        mockMvc.perform(get("/api/v1/test/conflict").with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @WithMockUser
    void data_integrity_violation_maps_to_409() throws Exception {
        mockMvc.perform(get("/api/v1/test/data-integrity-violation").with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }
}
