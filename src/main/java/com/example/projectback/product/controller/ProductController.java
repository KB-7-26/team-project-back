package com.example.projectback.product.controller;

import com.example.projectback.product.dto.ProductCreateRequest;
import com.example.projectback.product.dto.ProductCreateResponse;
import com.example.projectback.product.dto.ProductDetailResponse;
import com.example.projectback.product.dto.ProductImageUploadResponse;
import com.example.projectback.product.dto.ProductListResponse;
import com.example.projectback.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
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

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ProductImageUploadResponse>> uploadImages(@PathVariable Long id, @RequestPart("images") List<MultipartFile> files){
        return ResponseEntity.ok(productService.uploadImages(id,files));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id, @PathVariable Long imageId){
        productService.deleteImage(id, imageId);
        return ResponseEntity.noContent().build();
    }
}