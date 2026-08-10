package com.architek.oikos.document.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.document.application.dto.DocumentContentView;
import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.document.application.port.in.DeleteDocumentUseCase;
import com.architek.oikos.document.application.port.in.DownloadDocumentUseCase;
import com.architek.oikos.document.application.port.in.GetDocumentUseCase;
import com.architek.oikos.document.application.port.in.ListDocumentsByOwnerUseCase;
import com.architek.oikos.document.application.port.in.UploadDocumentUseCase;
import com.architek.oikos.document.domain.valueobject.DocumentId;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = DocumentController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class DocumentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UploadDocumentUseCase uploadDocumentUseCase;

    @MockitoBean
    private ListDocumentsByOwnerUseCase listDocumentsByOwnerUseCase;

    @MockitoBean
    private GetDocumentUseCase getDocumentUseCase;

    @MockitoBean
    private DownloadDocumentUseCase downloadDocumentUseCase;

    @MockitoBean
    private DeleteDocumentUseCase deleteDocumentUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    private DocumentView aDocumentView(EntityId ownerId) {
        return new DocumentView(DocumentId.newId(), DocumentOwnerType.PROPERTY, ownerId, "notice.pdf",
                "application/pdf", 42L, EntityId.newId(), Instant.now());
    }

    @Test
    void anonymous_upload_is_rejected_with_401() throws Exception {
        mockMvc.perform(multipart("/api/v1/documents")
                        .file(new MockMultipartFile("file", "notice.pdf", "application/pdf", new byte[] {1}))
                        .param("ownerType", "PROPERTY")
                        .param("ownerId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regular_user_without_grants_is_forbidden_from_uploading() throws Exception {
        mockMvc.perform(multipart("/api/v1/documents")
                        .file(new MockMultipartFile("file", "notice.pdf", "application/pdf", new byte[] {1}))
                        .param("ownerType", "PROPERTY")
                        .param("ownerId", UUID.randomUUID().toString())
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_upload_a_document() throws Exception {
        EntityId ownerId = EntityId.newId();
        when(uploadDocumentUseCase.upload(any())).thenReturn(aDocumentView(ownerId));

        mockMvc.perform(multipart("/api/v1/documents")
                        .file(new MockMultipartFile("file", "notice.pdf", "application/pdf", new byte[] {1, 2, 3}))
                        .param("ownerType", "PROPERTY")
                        .param("ownerId", ownerId.toString())
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isCreated());
    }

    @Test
    void admin_can_list_documents_for_an_owner() throws Exception {
        EntityId ownerId = EntityId.newId();
        when(listDocumentsByOwnerUseCase.list(any())).thenReturn(Page.of(java.util.List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/documents")
                        .param("ownerType", "PROPERTY")
                        .param("ownerId", ownerId.toString())
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_download_a_document_and_gets_a_content_disposition_header() throws Exception {
        DocumentId documentId = DocumentId.newId();
        when(getDocumentUseCase.getDocument(any())).thenReturn(aDocumentView(EntityId.newId()));
        when(downloadDocumentUseCase.download(any()))
                .thenReturn(new DocumentContentView("notice.pdf", "application/pdf", 3L, new byte[] {1, 2, 3}));

        mockMvc.perform(get("/api/v1/documents/" + documentId + "/content")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("notice.pdf")));
    }

    @Test
    void admin_can_delete_a_document() throws Exception {
        DocumentId documentId = DocumentId.newId();
        when(getDocumentUseCase.getDocument(any())).thenReturn(aDocumentView(EntityId.newId()));

        mockMvc.perform(delete("/api/v1/documents/" + documentId)
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isNoContent());
    }
}
