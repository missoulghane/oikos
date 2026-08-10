package com.architek.oikos.document.application.port.out;

/**
 * Storage-agnostic binary content port, free of any business concept (same
 * spirit as shared.application.port.out.EmailSenderPort): the calling use
 * case only ever deals with an opaque storageKey. Today's only
 * implementation is LocalDiskFileStorageAdapter (disk, gated by
 * oikos.storage.type=local); swapping to a cloud object store later means
 * adding a new adapter implementing this same contract, gated by
 * oikos.storage.type=cloud - no change needed here or in any use case.
 */
public interface FileStoragePort {

    void store(String storageKey, byte[] content);

    byte[] retrieve(String storageKey);

    void delete(String storageKey);
}
