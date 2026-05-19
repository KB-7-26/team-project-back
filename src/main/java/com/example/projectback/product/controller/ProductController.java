package com.example.projectback.product.controller;

import com.example.projectback.product.dto.ProductCreateRequest;
import com.example.projectback.product.dto.ProductCreateResponse;
import com.example.projectback.product.dto.ProductListResponse;
import com.example.projectback.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<Page<ProductListResponse>> getProducts(
            @RequestParam(required = false)
            Long categoryId,

            @RequestParam(defaultValue = "available")
            String saleStatus,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(productService.getProducts(categoryId, saleStatus, pageable));
    }
}