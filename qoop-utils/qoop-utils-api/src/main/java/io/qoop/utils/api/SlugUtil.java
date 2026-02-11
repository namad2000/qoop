package io.qoop.utils.api;

public class SlugUtil {

    public static String toSlug(String input) {
        if (input == null) return null;

        return input.trim()
                .replace("ي", "ی")
                .replace("ك", "ک")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\u0600-\\u06FF\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-");
    }
}
