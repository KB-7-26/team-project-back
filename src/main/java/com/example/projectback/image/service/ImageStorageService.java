package com.example.projectback.image.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    String store(MultipartFile file);
    void delete(String imageUrl);
}
