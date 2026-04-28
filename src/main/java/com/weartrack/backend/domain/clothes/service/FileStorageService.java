package com.weartrack.backend.domain.clothes.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public SavedFile save(MultipartFile image) {
        validateImage(image);

        try {
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = image.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String savedFilename = UUID.randomUUID() + extension;

            Path savedPath = uploadPath.resolve(savedFilename);

            Files.copy(
                    image.getInputStream(),
                    savedPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            String imageUrl = "/" + uploadDir + "/" + savedFilename;

            return new SavedFile(imageUrl, savedPath.toFile());

        } catch (IOException e) {
            throw new IllegalArgumentException("이미지 저장 중 오류가 발생했습니다.");
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일은 필수입니다.");
        }

        String contentType = image.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }

        return filename.substring(filename.lastIndexOf("."));
    }

    @Getter
    public static class SavedFile {
        private final String imageUrl;
        private final File file;

        public SavedFile(String imageUrl, File file) {
            this.imageUrl = imageUrl;
            this.file = file;
        }
    }
}