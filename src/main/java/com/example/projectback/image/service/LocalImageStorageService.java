package com.example.projectback.image.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalImageStorageService implements ImageStorageService{
    private final StandardServletMultipartResolver standardServletMultipartResolver;
    @Value("${image.upload-dir}")
    private String uploadDir;

    @Value("${image.base-url}")
    private String baseUrl;

    public LocalImageStorageService(StandardServletMultipartResolver standardServletMultipartResolver) {
        this.standardServletMultipartResolver = standardServletMultipartResolver;
    }


    @Override
    public String store(MultipartFile file) {
        if (file.isEmpty()){
            throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다.");
        }

        String extension = extractExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;
        Path targetPath = Path.of(uploadDir).resolve(filename);

        try{
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(),targetPath, StandardCopyOption.REPLACE_EXISTING);

        }catch (IOException e){
            throw new RuntimeException("파일 저장에 실패했습니다 : " + filename, e);
        }
        return baseUrl + "/uploads/" + filename;
    }

    @Override
    public void delete(String imageUrl) {
        String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        Path filePath = Path.of(uploadDir).resolve(filename);

        try{
            Files.deleteIfExists(filePath);
        }catch (IOException e){
            throw new RuntimeException("파일 삭제에 실패했습니다 : " + filename, e);
        }
    }

    private String extractExtension(String originalFilename){
        if (originalFilename == null || !originalFilename.contains(".")){
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }
}
