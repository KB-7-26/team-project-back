package com.example.projectback.user.controller;

import com.example.projectback.product.dto.ProductListResponse;
import com.example.projectback.user.service.UserMarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
public class UserMarketController {
    private final UserMarketService userMarketService;

    @GetMapping("/products")
    public ResponseEntity<Page<ProductListResponse>> getMyProducts(
            @RequestParam(required = false, defaultValue = "all") String saleStatus,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(userMarketService.getMyProducts(saleStatus, pageable));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<ProductListResponse>> getMyFavorites() {
        return ResponseEntity.ok(userMarketService.getMyFavorites());
    }

    @GetMapping("/purchases")
    public ResponseEntity<Page<ProductListResponse>> getMyPurchases(
            @PageableDefault(sort = "completedAt", direction = Sort.Direction.DESC, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(userMarketService.getMyPurchases(pageable));
    }
}
