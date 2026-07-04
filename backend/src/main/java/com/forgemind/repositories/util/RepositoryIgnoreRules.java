package com.forgemind.repositories.util;

import java.util.Set;

public final class RepositoryIgnoreRules {

    private RepositoryIgnoreRules() {
    }

    private static final Set<String> IGNORED_FOLDERS = Set.of(
            "node_modules",
            "target",
            "build",
            "dist",
            ".git",
            ".idea",
            ".vscode",
            "coverage",
            "vendor",
            "__pycache__",
            ".next",
            ".nuxt",
            ".cache",
            ".gradle",
            "out",
            "bin"
    );

    private static final Set<String> IGNORED_FILES = Set.of(
            ".ds_store",
            "thumbs.db",
            "desktop.ini"
    );

    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "webp", "ico", "svg",
            "mp4", "mov", "avi", "mkv", "mp3", "wav",
            "pdf", "zip", "rar", "7z", "tar", "gz",
            "class", "jar", "war", "ear",
            "exe", "dll", "so", "dylib",
            "ttf", "otf", "woff", "woff2",
            "lock"
    );

    public static boolean shouldIgnore(String path, boolean directory) {
        if (path == null || path.isBlank()) {
            return true;
        }

        String normalized = normalize(path);
        String[] parts = normalized.split("/");

        for (String part : parts) {
            String lower = part.toLowerCase();

            if (IGNORED_FOLDERS.contains(lower)) {
                return true;
            }

            if (lower.startsWith(".env")) {
                return true;
            }

            if (lower.endsWith(".pem")
                    || lower.endsWith(".key")
                    || lower.endsWith(".p12")
                    || lower.endsWith(".jks")) {
                return true;
            }
        }

        if (!directory) {
            String filename = parts[parts.length - 1].toLowerCase();

            if (IGNORED_FILES.contains(filename)) {
                return true;
            }

            String extension = getExtension(filename);

            return IGNORED_EXTENSIONS.contains(extension);
        }

        return false;
    }

    public static String normalize(String path) {
        return path
                .replace("\\", "/")
                .replaceAll("^/+", "")
                .replaceAll("/+", "/");
    }

    private static String getExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return "";
        }
        return filename.substring(index + 1);
    }
}