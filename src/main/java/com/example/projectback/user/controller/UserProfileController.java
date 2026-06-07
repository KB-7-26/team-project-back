package com.example.projectback.user.controller;

import com.example.projectback.common.ApiResponse;
import com.example.projectback.user.dto.UserProfileResponse;
import com.example.projectback.user.dto.UserProfileUpdateRequest;
import com.example.projectback.user.dto.UserPublicProfileResponse;
import com.example.projectback.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        UserProfileResponse response = userProfileService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.success(response, "내 프로필 조회 성공"));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<UserPublicProfileResponse>> getUserProfile(@PathVariable Long userId) {
        UserPublicProfileResponse response = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "사용자 프로필 조회 성공"));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        UserProfileResponse response = userProfileService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success(response, "내 프로필 수정 성공"));
    }

    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadProfileImage(@RequestPart("image") MultipartFile image) {
        UserProfileResponse response = userProfileService.uploadMyProfileImage(image);
        return ResponseEntity.ok(ApiResponse.success(response, "프로필 이미지 수정 성공"));
    }

    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<UserProfileResponse>> deleteProfileImage() {
        UserProfileResponse response = userProfileService.deleteMyProfileImage();
        return ResponseEntity.ok(ApiResponse.success(response, "프로필 이미지 삭제 성공"));
    }
}
