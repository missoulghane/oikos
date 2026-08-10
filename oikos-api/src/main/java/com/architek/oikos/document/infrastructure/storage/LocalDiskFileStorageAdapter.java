package com.architek.oikos.document.infrastructure.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.architek.oikos.document.application.port.out.FileStoragePort;

/**
 * Local-disk implementation of FileStoragePort. storageKey is always a
 * server-generated DocumentId (see Document.create), never derived from a
 * user-supplied file name, so it is safe to use directly as a file name
 * under baseDir with no further sanitization - there is no path-traversal
 * surface. Default/only adapter today (oikos.storage.type=local); a future
 * cloud adapter takes over via oikos.storage.type=cloud without touching
 * this class or any use case.
 */
@Component
@ConditionalOnProperty(prefix = "oikos.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalDiskFileStorageAdapter implements FileStoragePort {

    @Value("${oikos.storage.local.base-dir}")
    private String baseDir;

    @Override
    public void store(String storageKey, byte[] content) {
        try {
            Path dir = Path.of(baseDir);
            Files.createDirectories(dir);
            Files.write(dir.resolve(storageKey), content, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file for key " + storageKey, e);
        }
    }

    @Override
    public byte[] retrieve(String storageKey) {
        try {
            return Files.readAllBytes(Path.of(baseDir).resolve(storageKey));
        } catch (IOException e) {
            throw new FileStorageException("Failed to retrieve file for key " + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(Path.of(baseDir).resolve(storageKey));
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file for key " + storageKey, e);
        }
    }
}
