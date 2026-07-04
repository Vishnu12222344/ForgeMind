package com.forgemind.common.util;

import java.text.Normalizer;

public final class SlugUtils {

    private SlugUtils() {
    }

    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "project";
        }

        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD);

        String slug = normalized
                .replaceAll("[^\\p{Alnum}]+", "-")
                .toLowerCase()
                .replaceAll("^-+|-+$", "");

        return slug.isBlank() ? "project" : slug;
    }
}