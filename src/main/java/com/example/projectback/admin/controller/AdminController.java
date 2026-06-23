package com.example.projectback.admin.controller;

import com.example.projectback.admin.dto.AdminReportListResponse;
import com.example.projectback.admin.dto.AdminUserResponse;
import com.example.projectback.admin.service.AdminService;
import com.example.projectback.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUsers(pageable), "유저 목록 조회 성공"));
    }

    @PatchMapping("/users/{id}/suspend")
    public ResponseEntity<ApiResponse<AdminUserResponse>> suspendUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.toggleSuspend(id), "유저 정지/해제 성공"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.forceWithdraw(id);
        return ResponseEntity.ok(ApiResponse.success(null, "유저 강제 탈퇴 성공"));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<AdminReportListResponse>> getReports() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getReports(), "신고 목록 조회 성공"));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        adminService.adminDeletePost(id);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글 삭제 성공"));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        adminService.adminDeleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "상품 삭제 성공"));
    }
}