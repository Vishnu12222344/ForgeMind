package com.forgemind.repositories.parser;

import com.forgemind.common.exception.BadRequestException;
import com.forgemind.repositories.entity.RepositoryFile;
import com.forgemind.repositories.entity.RepositoryFileType;
import com.forgemind.repositories.entity.SourceRepository;
import com.forgemind.repositories.repository.RepositoryFileRepository;
import com.forgemind.repositories.util.BinaryFileDetector;
import com.forgemind.repositories.util.LanguageDetector;
import com.forgemind.repositories.util.RepositoryIgnoreRules;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@RequiredArgsConstructor
public class ZipRepositoryParser {

    private final RepositoryFileRepository repositoryFileRepository;

    @Value("${app.repository.max-stored-file-size-kb:1024}")
    private long maxStoredFileSizeKb;

    @Value("${app.repository.max-uncompressed-size-mb:250}")
    private long maxUncompressedSizeMb;

    public ParsedRepositoryResult parse(MultipartFile zipFile, SourceRepository repository) {
        Map<String, Long> languageStats = new HashMap<>();
        Set<String> folderPaths = new HashSet<>();

        long[] totalFiles = {0};
        long[] totalFolders = {0};
        long[] totalSizeBytes = {0};
        long[] totalUncompressedBytes = {0};

        long maxStoredBytes = maxStoredFileSizeKb * 1024;
        long maxUncompressedBytes = maxUncompressedSizeMb * 1024 * 1024;

        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String path = RepositoryIgnoreRules.normalize(entry.getName());

                validateZipPath(path);

                if (path.length() > 512) {
                    zis.closeEntry();
                    continue;
                }

                if (path.isBlank() || path.startsWith("__MACOSX")) {
                    zis.closeEntry();
                    continue;
                }

                boolean directory = entry.isDirectory() || path.endsWith("/");

                if (RepositoryIgnoreRules.shouldIgnore(path, directory)) {
                    zis.closeEntry();
                    continue;
                }

                path = path.replaceAll("/$", "");

                if (directory) {
                    if (folderPaths.add(path)) {
                        saveFolder(repository, path);
                        totalFolders[0]++;
                    }

                    zis.closeEntry();
                    continue;
                }

                ensureParentFolders(repository, path, folderPaths, totalFolders);

                FileReadResult readResult = readEntryBytes(zis, maxStoredBytes);
                totalUncompressedBytes[0] += readResult.totalBytes();

                if (totalUncompressedBytes[0] > maxUncompressedBytes) {
                    throw new BadRequestException("ZIP file is too large after extraction");
                }

                byte[] storedBytes = readResult.storedBytes();
                boolean binary = readResult.truncated()
                        || BinaryFileDetector.isBinary(storedBytes);

                String language = LanguageDetector.detectLanguage(path);
                String extension = LanguageDetector.getExtension(path);
                String name = LanguageDetector.getFilename(path);

                String content = null;

                if (!binary && storedBytes != null) {
                    content = new String(storedBytes, StandardCharsets.UTF_8);
                }

                RepositoryFile repositoryFile = RepositoryFile.builder()
                        .repository(repository)
                        .type(RepositoryFileType.FILE)
                        .path(path)
                        .name(name)
                        .extension(extension)
                        .language(language)
                        .sizeBytes(readResult.totalBytes())
                        .depth(calculateDepth(path))
                        .binaryFile(binary)
                        .contentTruncated(readResult.truncated())
                        .content(content)
                        .build();

                repositoryFileRepository.save(repositoryFile);

                totalFiles[0]++;
                totalSizeBytes[0] += readResult.totalBytes();

                languageStats.put(language, languageStats.getOrDefault(language, 0L) + 1);

                zis.closeEntry();
            }
        } catch (IOException ex) {
            throw new BadRequestException("Failed to parse ZIP file: " + ex.getMessage());
        }

        String primaryLanguage = detectPrimaryLanguage(languageStats);

        return ParsedRepositoryResult.builder()
                .totalFiles(totalFiles[0])
                .totalFolders(totalFolders[0])
                .totalSizeBytes(totalSizeBytes[0])
                .primaryLanguage(primaryLanguage)
                .languageStats(languageStats)
                .build();
    }

    private void validateZipPath(String path) {
        if (path.contains("..") || path.startsWith("/") || path.startsWith("\\")) {
            throw new BadRequestException("Invalid ZIP entry path detected");
        }
    }

    private void ensureParentFolders(
            SourceRepository repository,
            String filePath,
            Set<String> folderPaths,
            long[] totalFolders
    ) {
        int lastSlash = filePath.lastIndexOf('/');

        if (lastSlash < 0) {
            return;
        }

        String parentPath = filePath.substring(0, lastSlash);
        String[] parts = parentPath.split("/");

        StringBuilder currentPath = new StringBuilder();

        for (String part : parts) {
            if (currentPath.length() > 0) {
                currentPath.append("/");
            }

            currentPath.append(part);

            String folderPath = currentPath.toString();

            if (folderPaths.add(folderPath)) {
                saveFolder(repository, folderPath);
                totalFolders[0]++;
            }
        }
    }

    private void saveFolder(SourceRepository repository, String path) {
        RepositoryFile folder = RepositoryFile.builder()
                .repository(repository)
                .type(RepositoryFileType.FOLDER)
                .path(path)
                .name(LanguageDetector.getFilename(path))
                .extension("")
                .language(null)
                .sizeBytes(0)
                .depth(calculateDepth(path))
                .binaryFile(false)
                .contentTruncated(false)
                .content(null)
                .build();

        repositoryFileRepository.save(folder);
    }

    private FileReadResult readEntryBytes(ZipInputStream zis, long maxStoredBytes) throws IOException {
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        long totalBytes = 0;
        boolean truncated = false;

        int read;

        while ((read = zis.read(buffer)) != -1) {
            totalBytes += read;

            if (totalBytes <= maxStoredBytes) {
                outputStream.write(buffer, 0, read);
            } else {
                truncated = true;
            }
        }

        byte[] storedBytes = truncated ? null : outputStream.toByteArray();

        return new FileReadResult(storedBytes, totalBytes, truncated);
    }

    private int calculateDepth(String path) {
        if (path == null || path.isBlank()) {
            return 0;
        }

        return path.split("/").length - 1;
    }

    private String detectPrimaryLanguage(Map<String, Long> languageStats) {
        return languageStats.entrySet()
                .stream()
                .filter(entry -> !"Text".equalsIgnoreCase(entry.getKey()))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseGet(() -> languageStats.keySet().stream().findFirst().orElse(null));
    }
}