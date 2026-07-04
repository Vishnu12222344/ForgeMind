package com.forgemind.repositories.parser;

public record FileReadResult(
        byte[] storedBytes,
        long totalBytes,
        boolean truncated
) {
}