package com.architek.oikos.document.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.document.domain.model.Document;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.document.infrastructure.mapper.DocumentPersistenceMapperImpl;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DocumentRepositoryAdapter.class, DocumentPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class DocumentRepositoryAdapterDataJpaTest {

    @Autowired
    private DocumentRepositoryAdapter adapter;

    private Document aDocument(DocumentOwnerType ownerType, EntityId ownerId, String checksum) {
        return Document.create(ownerType, ownerId, "notice.pdf", "application/pdf", 42L, checksum, EntityId.newId());
    }

    @Test
    void saves_and_finds_a_document_by_id() {
        Document document = aDocument(DocumentOwnerType.PROPERTY, EntityId.newId(), "checksum-1");

        adapter.save(document);

        assertThat(adapter.findById(document.getId())).isPresent()
                .get().extracting(Document::getFileName).isEqualTo("notice.pdf");
    }

    @Test
    void findAllByOwner_paginates_and_filters_by_owner() {
        EntityId ownerId = EntityId.newId();
        for (int i = 0; i < 3; i++) {
            adapter.save(aDocument(DocumentOwnerType.PROPERTY, ownerId, "checksum-" + i));
        }
        adapter.save(aDocument(DocumentOwnerType.PROPERTY, EntityId.newId(), "checksum-other-owner"));
        adapter.save(aDocument(DocumentOwnerType.UNIT, ownerId, "checksum-other-type"));

        var page = adapter.findAllByOwner(DocumentOwnerType.PROPERTY, ownerId, PageRequest.of(0, 2));

        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.content()).hasSize(2);
    }

    @Test
    void existsByOwnerAndChecksum_reflects_persisted_state() {
        EntityId ownerId = EntityId.newId();
        assertThat(adapter.existsByOwnerAndChecksum(DocumentOwnerType.PROPERTY, ownerId, "checksum-1")).isFalse();

        adapter.save(aDocument(DocumentOwnerType.PROPERTY, ownerId, "checksum-1"));

        assertThat(adapter.existsByOwnerAndChecksum(DocumentOwnerType.PROPERTY, ownerId, "checksum-1")).isTrue();
        assertThat(adapter.existsByOwnerAndChecksum(DocumentOwnerType.UNIT, ownerId, "checksum-1")).isFalse();
    }

    @Test
    void deleteById_removes_the_document() {
        Document document = aDocument(DocumentOwnerType.PROPERTY, EntityId.newId(), "checksum-1");
        adapter.save(document);

        adapter.deleteById(document.getId());

        assertThat(adapter.findById(document.getId())).isEmpty();
    }
}
