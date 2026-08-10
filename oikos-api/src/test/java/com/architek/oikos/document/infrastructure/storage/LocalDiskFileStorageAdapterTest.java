package com.architek.oikos.document.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

class LocalDiskFileStorageAdapterTest {

    @TempDir
    Path tempDir;

    private LocalDiskFileStorageAdapter newAdapter() {
        LocalDiskFileStorageAdapter adapter = new LocalDiskFileStorageAdapter();
        ReflectionTestUtils.setField(adapter, "baseDir", tempDir.toString());
        return adapter;
    }

    @Test
    void stores_and_retrieves_the_same_content() {
        LocalDiskFileStorageAdapter adapter = newAdapter();
        byte[] content = {1, 2, 3, 4, 5};

        adapter.store("key-1", content);

        assertThat(adapter.retrieve("key-1")).isEqualTo(content);
    }

    @Test
    void store_creates_the_base_directory_if_missing() throws IOException {
        Path nested = tempDir.resolve("nested/sub-dir");
        LocalDiskFileStorageAdapter adapter = new LocalDiskFileStorageAdapter();
        ReflectionTestUtils.setField(adapter, "baseDir", nested.toString());

        adapter.store("key-1", new byte[] {1});

        assertThat(Files.exists(nested.resolve("key-1"))).isTrue();
    }

    @Test
    void delete_removes_the_stored_file() {
        LocalDiskFileStorageAdapter adapter = newAdapter();
        adapter.store("key-1", new byte[] {1});

        adapter.delete("key-1");

        assertThat(Files.exists(tempDir.resolve("key-1"))).isFalse();
    }

    @Test
    void deleting_a_missing_key_is_a_no_op() {
        LocalDiskFileStorageAdapter adapter = newAdapter();

        assertThatCode(() -> adapter.delete("does-not-exist")).doesNotThrowAnyException();
    }

    @Test
    void retrieving_a_missing_key_fails_with_a_storage_exception() {
        LocalDiskFileStorageAdapter adapter = newAdapter();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> adapter.retrieve("does-not-exist"))
                .isInstanceOf(FileStorageException.class);
    }
}
