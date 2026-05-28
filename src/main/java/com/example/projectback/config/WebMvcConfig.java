package com.example.projectback.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${image.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        String absolutePath = Path.of(uploadDir).toAbsolutePath().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations("file: " + absolutePath + "/");
    }
}
