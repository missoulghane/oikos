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

import com.architek.oikos.meeting.application.command.ConfirmConvocationByCodeCommand;
import com.architek.oikos.meeting.application.command.ConfirmConvocationByTokenCommand;
import com.architek.oikos.meeting.application.dto.ConvocationConfirmationView;
import com.architek.oikos.meeting.application.query.GetConvocationByCodeQuery;
import com.architek.oikos.meeting.application.port.in.ConfirmConvocationByCodeUseCase;
import com.architek.oikos.meeting.application.port.in.ConfirmConvocationByTokenUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationByCodeUseCase;
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

    @MockitoBean
    private GetConvocationByCodeUseCase getConvocationByCodeUseCase;

    @MockitoBean
    private ConfirmConvocationByCodeUseCase confirmConvocationByCodeUseCase;

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
                        .content("{\"attendanceReply\":\"ATTENDING\",\"confirmationCode\":\"W754A1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendanceReply").value("ATTENDING"));

        ArgumentCaptor<ConfirmConvocationByTokenCommand> captor =
                ArgumentCaptor.forClass(ConfirmConvocationByTokenCommand.class);
        verify(confirmConvocationByTokenUseCase).confirm(captor.capture());
        assertThat(captor.getValue().token()).isEqualTo("some-token");
        assertThat(captor.getValue().attendanceReply()).isEqualTo(AttendanceReply.ATTENDING);
        // Read off paper, where it is printed in capitals - the same normalisation as the
        // paper path, or half the copropriétaires would be told their own code is wrong.
        assertThat(captor.getValue().confirmationCode().value()).isEqualTo("w754a1");
        // Deduced from the request, never taken from the body: it feeds the attempt cap.
        assertThat(captor.getValue().callerId()).isNotBlank();
    }

    @Test
    void a_body_without_an_answer_is_rejected_before_reaching_the_use_case() throws Exception {
        mockMvc.perform(put("/api/v1/convocations/by-token/some-token/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmationCode\":\"w754a1\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(confirmConvocationByTokenUseCase);
    }

    @Test
    void an_answer_without_the_lot_code_never_reaches_the_use_case() throws Exception {
        // The link alone no longer answers for a lot (ADR 0002 §16). Pinned at the web layer
        // because dropping the field from the body is exactly how the check would come undone:
        // the page would keep working, and nothing would fail.
        mockMvc.perform(put("/api/v1/convocations/by-token/some-token/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"attendanceReply\":\"ATTENDING\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(confirmConvocationByTokenUseCase);
    }

    @Test
    void the_pair_of_codes_reaches_the_same_confirmation_anonymously() throws Exception {
        // The path for a paper letter: six characters and six characters, no token, no account.
        when(confirmConvocationByCodeUseCase.confirm(any())).thenReturn(view(AttendanceReply.ATTENDING, true));

        mockMvc.perform(put("/api/v1/convocations/by-token/by-reference/x7k2m9/w754a1/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"attendanceReply\":\"ATTENDING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendanceReply").value("ATTENDING"));

        ArgumentCaptor<ConfirmConvocationByCodeCommand> captor =
                ArgumentCaptor.forClass(ConfirmConvocationByCodeCommand.class);
        verify(confirmConvocationByCodeUseCase).confirm(captor.capture());
        assertThat(captor.getValue().meetingReference().value()).isEqualTo("x7k2m9");
        assertThat(captor.getValue().confirmationCode().value()).isEqualTo("w754a1");
        // Deduced from the request, never taken from the body: a caller able to state its own
        // identity would be handed the means to reset its own attempt counter.
        assertThat(captor.getValue().callerId()).isNotBlank();
    }

    @Test
    void a_code_typed_in_capitals_is_the_same_code() throws Exception {
        // It is read off paper, where it is printed in capitals for legibility.
        when(getConvocationByCodeUseCase.getByCode(any())).thenReturn(view(AttendanceReply.NO_REPLY, true));

        mockMvc.perform(get("/api/v1/convocations/by-token/by-reference/X7K2M9/W754A1"))
                .andExpect(status().isOk());

        ArgumentCaptor<GetConvocationByCodeQuery> captor = ArgumentCaptor.forClass(GetConvocationByCodeQuery.class);
        verify(getConvocationByCodeUseCase).getByCode(captor.capture());
        assertThat(captor.getValue().meetingReference().value()).isEqualTo("x7k2m9");
        assertThat(captor.getValue().confirmationCode().value()).isEqualTo("w754a1");
    }

    @Test
    void a_code_of_the_wrong_length_is_refused_without_reaching_the_use_case() throws Exception {
        mockMvc.perform(get("/api/v1/convocations/by-token/by-reference/x7k2m9/toolong"))
                .andExpect(status().is4xxClientError());

        verifyNoInteractions(getConvocationByCodeUseCase);
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
