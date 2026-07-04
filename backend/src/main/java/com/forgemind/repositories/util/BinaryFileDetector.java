package com.forgemind.repositories.util;

public final class BinaryFileDetector {

    private BinaryFileDetector() {
    }

    public static boolean isBinary(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }

        int sampleSize = Math.min(bytes.length, 8000);

        for (int i = 0; i < sampleSize; i++) {
            if (bytes[i] == 0) {
                return true;
            }
        }

        return false;
    }
}