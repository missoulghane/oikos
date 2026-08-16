package com.architek.oikos.meeting.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.meeting.application.command.ConfirmConvocationByTokenCommand;
import com.architek.oikos.meeting.application.dto.ConvocationConfirmationView;
import com.architek.oikos.meeting.application.port.in.ConfirmConvocationByTokenUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationByTokenUseCase;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.VenueType;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

/**
 * The one place where the security rule itself is under test rather than the
 * controller: WebSecuritySliceTestConfiguration brings in the real
 * SecurityConfiguration, so these calls go through the actual filter chain.
 *
 * <p>What is being pinned is unusual enough to deserve its own test - an
 * anonymous <em>write</em>. Every other endpoint of the product requires a
 * principal; this one deliberately does not, because it exists for the
 * copropriétaires who have no account. If someone later tightens the matcher to
 * GET-only (as the invitation flow above it is), the reply endpoint would start
 * returning 401 to exactly the people it was built for - and nobody would find
 * out from a bug report, since those people have no other way in either.
 */
@WebMvcTest(controllers = PublicConvocationController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class PublicConvocationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetConvocationByTokenUseCase getConvocationByTokenUseCase;

    @MockitoBean
    private ConfirmConvocationByTokenUseCase confirmConvocationByTokenUseCase;

    private static ConvocationConfirmationView view(AttendanceReply reply, boolean stillOpen) {
        return new ConvocationConfirmationView("Résidence Al Amal", "AG ordinaire 2026", "ORDINARY",
                Instant.parse("2026-09-15T17:00:00Z"), VenueType.PHYSICAL, "12 rue des Orangers", null,
                "Appartement 1", "Bâtiment A", reply, null, stillOpen);
    }

    @Test
    void the_preview_is_readable_without_authenticating() throws Exception {
        when(getConvocationByTokenUseCase.getByToken(any())).thenReturn(view(AttendanceReply.NO_REPLY, true));

        mockMvc.perform(get("/api/v1/convocations/by-token/some-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitNumber").value("Appartement 1"))
                .andExpect(jsonPath("$.stillOpen").value(true));
    }

    @Test
    void the_answer_is_writable_without_authenticating() throws Exception {
        when(confirmConvocationByTokenUseCase.confirm(any())).thenReturn(view(AttendanceReply.ATTENDING, true));

        mockMvc.perform(put("/api/v1/convocations/by-token/some-token/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"attendanceReply\":\"ATTENDING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendanceReply").value("ATTENDING"));

        ArgumentCaptor<ConfirmConvocationByTokenCommand> captor =
                ArgumentCaptor.forClass(ConfirmConvocationByTokenCommand.class);
        verify(confirmConvocationByTokenUseCase).confirm(captor.capture());
        assertThat(captor.getValue().token()).isEqualTo("some-token");
        assertThat(captor.getValue().attendanceReply()).isEqualTo(AttendanceReply.ATTENDING);
    }

    @Test
    void a_body_without_an_answer_is_rejected_before_reaching_the_use_case() throws Exception {
        mockMvc.perform(put("/api/v1/convocations/by-token/some-token/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(confirmConvocationByTokenUseCase);
    }

    @Test
    void the_response_carries_no_identifier_of_any_kind() throws Exception {
        // Not the convocation's, not the lot's, not the meeting's. The page is anonymous and
        // the token is the only handle it needs; an id would only be one more thing to leak.
        when(getConvocationByTokenUseCase.getByToken(any())).thenReturn(view(AttendanceReply.NO_REPLY, true));

        mockMvc.perform(get("/api/v1/convocations/by-token/some-token"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"id\""))))
                .andExpect(jsonPath("$.unitId").doesNotExist())
                .andExpect(jsonPath("$.generalMeetingId").doesNotExist());
    }
}
