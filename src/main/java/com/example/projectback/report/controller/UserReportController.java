package com.example.projectback.report.controller;

import com.example.projectback.common.ApiResponse;
import com.example.projectback.report.dto.UserReportCreateRequest;
import com.example.projectback.report.service.UserReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-reports")
@RequiredArgsConstructor
public class UserReportController {

    private final UserReportService userReportService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> reportProductSeller(
            @Valid @RequestBody UserReportCreateRequest request
    ) {
        userReportService.reportProductUser(request.getProductId(), request.getReportedUserId());
        return ResponseEntity.ok(ApiResponse.success("신고가 접수되었습니다."));
    }
}
