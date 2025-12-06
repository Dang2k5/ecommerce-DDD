package com.dang.productservice.infrastructure.util;

import java.text.Normalizer;

public class SlugUtil {
    public static String toSlug(String input) {
        if (input == null) return null;

        // Bỏ khoảng trắng 2 đầu và thay khoảng trắng giữa bằng dấu -
        String noWhitespace = input.trim().replaceAll("\\s+", "-");

        // Chuẩn hoá và remove dấu tiếng Việt
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = normalized
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "") // bỏ dấu
                .replaceAll("[^\\w-]", "") // bỏ ký tự lạ
                .toLowerCase();

        return slug;
    }

    private SlugUtil() {
        // utility class, không cho new
    }
}
