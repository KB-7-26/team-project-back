package com.example.projectback.product.controller;

import com.example.projectback.product.dto.ProductCreateRequest;
import com.example.projectback.product.dto.ProductCreateResponse;
import com.example.projectback.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductCreateResponse> createProduct(@RequestBody ProductCreateRequest request){
        ProductCreateResponse response = productService.createProduct(request);
        return ResponseEntity.ok(response);
    }
}