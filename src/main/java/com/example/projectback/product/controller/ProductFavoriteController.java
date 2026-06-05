package com.example.projectback.product.controller;

import com.example.projectback.product.service.ProductFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProductFavoriteController {
    private final ProductFavoriteService productFavoriteService;

    @PostMapping("/api/products/{id}/favorites")
    public ResponseEntity<Void> toggleFavorite(@PathVariable Long id) {
        productFavoriteService.toggleFavorite(id);
        return ResponseEntity.ok().build();
    }
}
