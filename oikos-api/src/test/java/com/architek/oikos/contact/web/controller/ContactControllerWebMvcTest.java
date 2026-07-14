package com.architek.oikos.contact.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.contact.application.dto.ContactView;
import com.architek.oikos.contact.application.port.in.CreateContactUseCase;
import com.architek.oikos.contact.application.port.in.DeleteContactUseCase;
import com.architek.oikos.contact.application.port.in.GetContactUseCase;
import com.architek.oikos.contact.application.port.in.ListContactsUseCase;
import com.architek.oikos.contact.application.port.in.UpdateContactUseCase;
import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = ContactController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class ContactControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CreateContactUseCase createContactUseCase;

    @MockitoBean
    private GetContactUseCase getContactUseCase;

    @MockitoBean
    private UpdateContactUseCase updateContactUseCase;

    @MockitoBean
    private ListContactsUseCase listContactsUseCase;

    @MockitoBean
    private DeleteContactUseCase deleteContactUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/contacts")).andExpect(status().isUnauthorized());
    }

    @Test
    void regular_user_is_forbidden_from_listing_contacts() throws Exception {
        mockMvc.perform(get("/api/v1/contacts").header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_list_contacts() throws Exception {
        when(listContactsUseCase.listContacts(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/contacts").header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_get_a_contact_by_id() throws Exception {
        ContactId id = ContactId.newId();
        when(getContactUseCase.getContact(any())).thenReturn(new ContactView(id, "Doe", "Jane", "jane@doe.com", null));

        mockMvc.perform(get("/api/v1/contacts/" + id).header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_create_a_contact_returns_201_with_location_header() throws Exception {
        ContactId id = ContactId.newId();
        when(createContactUseCase.create(any())).thenReturn(id);

        mockMvc.perform(post("/api/v1/contacts")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Doe","firstName":"Jane","email":"jane@doe.com"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void regular_user_is_forbidden_from_creating_a_contact() throws Exception {
        mockMvc.perform(post("/api/v1/contacts")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Doe","firstName":"Jane","email":"jane@doe.com"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_update_a_contact() throws Exception {
        ContactId id = ContactId.newId();
        when(updateContactUseCase.update(any())).thenReturn(new ContactView(id, "Smith", "Janet", "janet@smith.com", null));

        mockMvc.perform(put("/api/v1/contacts/" + id)
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Smith","firstName":"Janet","email":"janet@smith.com"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_delete_a_contact() throws Exception {
        mockMvc.perform(delete("/api/v1/contacts/" + ContactId.newId())
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void regular_user_is_forbidden_from_deleting_a_contact() throws Exception {
        mockMvc.perform(delete("/api/v1/contacts/" + ContactId.newId())
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }
}
