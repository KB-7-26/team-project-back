package com.example.projectback.product.controller;

import com.example.projectback.product.dto.*;
import com.example.projectback.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String saleStatus,
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10) Pageable pageable
    ){
        return ResponseEntity.ok(productService.getProducts(categoryId, saleStatus, keyword, pageable));
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

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateSaleStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        productService.updateSaleStatus(id, body.get("status"));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateRequest request){
        productService.updateProduct(id, request);
        return ResponseEntity.noContent().build();
    }
}