package com.forgemind.repositories.util;

import java.util.Map;

public final class LanguageDetector {

    private LanguageDetector() {
    }

    private static final Map<String, String> EXTENSION_LANGUAGE_MAP = Map.ofEntries(
            Map.entry("java", "Java"),
            Map.entry("kt", "Kotlin"),
            Map.entry("kts", "Kotlin"),
            Map.entry("js", "JavaScript"),
            Map.entry("jsx", "JavaScript"),
            Map.entry("ts", "TypeScript"),
            Map.entry("tsx", "TypeScript"),
            Map.entry("py", "Python"),
            Map.entry("go", "Go"),
            Map.entry("rs", "Rust"),
            Map.entry("cpp", "C++"),
            Map.entry("cc", "C++"),
            Map.entry("cxx", "C++"),
            Map.entry("c", "C"),
            Map.entry("h", "C/C++ Header"),
            Map.entry("hpp", "C++ Header"),
            Map.entry("cs", "C#"),
            Map.entry("php", "PHP"),
            Map.entry("rb", "Ruby"),
            Map.entry("swift", "Swift"),
            Map.entry("scala", "Scala"),
            Map.entry("html", "HTML"),
            Map.entry("css", "CSS"),
            Map.entry("scss", "SCSS"),
            Map.entry("sass", "Sass"),
            Map.entry("json", "JSON"),
            Map.entry("xml", "XML"),
            Map.entry("yml", "YAML"),
            Map.entry("yaml", "YAML"),
            Map.entry("md", "Markdown"),
            Map.entry("sql", "SQL"),
            Map.entry("sh", "Shell"),
            Map.entry("bat", "Batch"),
            Map.entry("ps1", "PowerShell"),
            Map.entry("dockerfile", "Dockerfile"),
            Map.entry("gradle", "Gradle"),
            Map.entry("properties", "Properties"),
            Map.entry("toml", "TOML")
    );

    public static String detectLanguage(String path) {
        String filename = getFilename(path).toLowerCase();

        if (filename.equals("dockerfile")) {
            return "Dockerfile";
        }

        if (filename.equals("makefile")) {
            return "Makefile";
        }

        String extension = getExtension(filename);

        if (extension.isBlank()) {
            return "Text";
        }

        return EXTENSION_LANGUAGE_MAP.getOrDefault(extension, "Text");
    }

    public static String getExtension(String path) {
        String filename = getFilename(path);
        int index = filename.lastIndexOf('.');

        if (index < 0 || index == filename.length() - 1) {
            return "";
        }

        return filename.substring(index + 1).toLowerCase();
    }

    public static String getFilename(String path) {
        String normalized = RepositoryIgnoreRules.normalize(path);
        int index = normalized.lastIndexOf('/');

        if (index < 0) {
            return normalized;
        }

        return normalized.substring(index + 1);
    }
}