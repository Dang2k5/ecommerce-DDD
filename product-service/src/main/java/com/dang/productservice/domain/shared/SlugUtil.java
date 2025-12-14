package com.dang.productservice.domain.shared;

import java.text.Normalizer;

public final class SlugUtil {

    private SlugUtil() {
    }

    public static String normalize(String input) {
        if (input == null) return null;

        String trimmed = input.trim();
        if (trimmed.isBlank()) return null;

        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        String slug = normalized.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");

        return slug.isBlank() ? null : slug;
    }

    public static String generateFromName(String name) {
        return normalize(name);
    }
}
