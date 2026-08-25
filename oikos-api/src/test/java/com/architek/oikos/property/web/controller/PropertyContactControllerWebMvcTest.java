package com.architek.oikos.property.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.property.application.port.in.FindContactByAccountEmailUseCase;
import com.architek.oikos.property.application.port.in.ListContactsByPropertyUseCase;
import com.architek.oikos.property.application.query.ListContactsByPropertyQuery;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = PropertyContactController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class PropertyContactControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ListContactsByPropertyUseCase listContactsByPropertyUseCase;

    @MockitoBean
    private FindContactByAccountEmailUseCase findContactByAccountEmailUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + PropertyId.newId() + "/contacts")).andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_list_contacts_of_a_property() throws Exception {
        when(listContactsByPropertyUseCase.listContacts(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/properties/" + PropertyId.newId() + "/contacts")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void the_account_filter_is_bound_from_the_query_string() throws Exception {
        when(listContactsByPropertyUseCase.listContacts(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/properties/" + PropertyId.newId() + "/contacts")
                        .param("hasLinkedAccount", "false")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());

        var captor = ArgumentCaptor.forClass(ListContactsByPropertyQuery.class);
        verify(listContactsByPropertyUseCase).listContacts(captor.capture());
        assertThat(captor.getValue().hasLinkedAccount()).isFalse();
    }
}
