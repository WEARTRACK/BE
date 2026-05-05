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

    @Value("${cloud.aws.region}")
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
        String extension = getExtension(originalFilename, image.getContentType());
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


    public void deleteByUrl(String imageUrl) {
        String key = extractKey(imageUrl);
        if (key != null) {
            delete(key);
        }
    }

    private String extractKey(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        String marker = ".amazonaws.com/";
        int idx = imageUrl.indexOf(marker);
        if (idx == -1) return null;
        return imageUrl.substring(idx + marker.length());
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

    private String getExtension(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();

            if (extension.equals(".jpg")
                    || extension.equals(".jpeg")
                    || extension.equals(".png")
                    || extension.equals(".webp")) {
                return extension;
            }

            throw new IllegalArgumentException("허용되지 않는 이미지 확장자입니다.");
        }

        if ("image/jpeg".equals(contentType)) {
            return ".jpg";
        }

        if ("image/png".equals(contentType)) {
            return ".png";
        }

        if ("image/webp".equals(contentType)) {
            return ".webp";
        }

        throw new IllegalArgumentException("이미지 확장자를 확인할 수 없습니다.");
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