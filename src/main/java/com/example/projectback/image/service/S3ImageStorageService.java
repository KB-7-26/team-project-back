package com.example.projectback.image.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3ImageStorageService implements ImageStorageService {

    private final S3Client s3Client;

    @Value("${s3.bucket-name}")
    private String bucketName;

    @Value("${s3.object-prefix:uploads}")
    private String objectPrefix;

    @Value("${s3.public-base-url}")
    private String publicBaseUrl;

    public S3ImageStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다.");
        }
        validateRequiredProperties();

        String objectKey = buildObjectKey(file.getOriginalFilename());
        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentLength(file.getSize());

        if (StringUtils.hasText(file.getContentType())) {
            requestBuilder.contentType(file.getContentType());
        }

        try {
            s3Client.putObject(
                    requestBuilder.build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new RuntimeException("S3 파일 업로드에 실패했습니다 : " + objectKey, e);
        }

        return normalizeBaseUrl(publicBaseUrl) + "/" + objectKey;
    }

    @Override
    public void delete(String imageUrl) {
        if (!StringUtils.hasText(bucketName)) {
            return;
        }

        String objectKey = extractObjectKey(imageUrl);
        if (!StringUtils.hasText(objectKey)) {
            return;
        }

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build());
    }

    private void validateRequiredProperties() {
        if (!StringUtils.hasText(bucketName)) {
            throw new IllegalStateException("S3 버킷 이름이 설정되지 않았습니다.");
        }
        if (!StringUtils.hasText(publicBaseUrl)) {
            throw new IllegalStateException("S3 public base URL이 설정되지 않았습니다.");
        }
    }

    private String buildObjectKey(String originalFilename) {
        String filename = UUID.randomUUID() + extractExtension(originalFilename);
        String normalizedPrefix = normalizePrefix(objectPrefix);
        return normalizedPrefix.isBlank() ? filename : normalizedPrefix + "/" + filename;
    }

    private String extractObjectKey(String imageUrl) {
        if (!StringUtils.hasText(imageUrl) || !StringUtils.hasText(publicBaseUrl)) {
            return "";
        }

        String normalizedBaseUrl = normalizeBaseUrl(publicBaseUrl) + "/";
        if (!imageUrl.startsWith(normalizedBaseUrl)) {
            return "";
        }
        return imageUrl.substring(normalizedBaseUrl.length());
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    private String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }
        return prefix.trim().replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.trim().replaceAll("/+$", "");
    }
}
