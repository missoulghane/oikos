package com.architek.oikos.document.application.dto;

public record DocumentContentView(String fileName, String contentType, long sizeBytes, byte[] content) {
}
