package com.weartrack.backend.domain.clothes.service;

import com.weartrack.backend.domain.closet.exception.ClosetErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public SavedImage uploadClothesImage(MultipartFile image) {
        return upload(image, "clothes");
    }

    public SavedImage uploadClosetImage(MultipartFile image) {
        return upload(image, "closets");
    }

    private SavedImage upload(MultipartFile image, String dirName) {
        validateImage(image);

        String originalFilename = image.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String key = dirName + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(image.getContentType())
                    .contentLength(image.getSize())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(image.getInputStream(), image.getSize())
            );

            String imageUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;

            return new SavedImage(key, imageUrl);

        } catch (IOException e) {
            throw new IllegalArgumentException("이미지 업로드 중 오류가 발생했습니다.");
        }
    }

    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        s3Client.deleteObject(builder -> builder
                .bucket(bucket)
                .key(key)
                .build());
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일은 필수입니다.");
        }

        String contentType = image.getContentType();

        if (contentType == null ||
                !(contentType.equals("image/jpeg")
                        || contentType.equals("image/png")
                        || contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("이미지 파일은 JPG, PNG, WebP 형식만 업로드할 수 있습니다.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }

        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();

        if (!extension.equals(".jpg")
                && !extension.equals(".jpeg")
                && !extension.equals(".png")
                && !extension.equals(".webp")) {
            throw new IllegalArgumentException("허용되지 않는 이미지 확장자입니다.");
        }

        return extension;
    }

    @Getter
    public static class SavedImage {
        private final String key;
        private final String imageUrl;

        public SavedImage(String key, String imageUrl) {
            this.key = key;
            this.imageUrl = imageUrl;
        }
    }
}