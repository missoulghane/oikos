package com.architek.oikos.document.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class DocumentTest {

    private static Document withTitle(String fileName, String title) {
        return Document.create(DocumentOwnerType.PROPERTY, EntityId.newId(), fileName, title, "application/pdf", 1L,
                "checksum", EntityId.newId());
    }

    @Test
    void a_given_title_is_kept_trimmed() {
        assertThat(withTitle("scan.pdf", "  Règlement  ").getTitle()).isEqualTo("Règlement");
    }

    @Test
    void without_a_title_the_file_name_without_its_extension_stands_in() {
        assertThat(withTitle("Budget 2026.pdf", null).getTitle()).isEqualTo("Budget 2026");
        assertThat(withTitle("archive.tar.gz", " ").getTitle()).isEqualTo("archive.tar");
        assertThat(withTitle(".env", null).getTitle()).isEqualTo(".env");
        assertThat(withTitle("README", null).getTitle()).isEqualTo("README");
    }

    @Test
    void an_over_long_file_name_is_cut_to_the_title_limit_rather_than_refused() {
        assertThat(withTitle("x".repeat(300) + ".pdf", null).getTitle()).hasSize(Document.TITLE_MAX_LENGTH);
    }

    @Test
    void an_over_long_title_is_refused() {
        assertThatThrownBy(() -> withTitle("scan.pdf", "x".repeat(Document.TITLE_MAX_LENGTH + 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
