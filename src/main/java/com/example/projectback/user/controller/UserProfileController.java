package com.example.projectback.user.controller;

import com.example.projectback.common.ApiResponse;
import com.example.projectback.user.dto.UserProfileResponse;
import com.example.projectback.user.dto.UserProfileUpdateRequest;
import com.example.projectback.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        UserProfileResponse response = userProfileService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.success(response, "내 프로필 조회 성공"));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        UserProfileResponse response = userProfileService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success(response, "내 프로필 수정 성공"));
    }

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> uploadProfileImage(@RequestPart("image") MultipartFile image) {
        // TODO: ImageStorageService를 사용해 로컬/S3 저장소에 업로드하고 users.profileImageUrl을 갱신한다.
        return ResponseEntity
                .status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.failure("프로필 이미지 업로드는 아직 구현 예정입니다."));
    }
}
