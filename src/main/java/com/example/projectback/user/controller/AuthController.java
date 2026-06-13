package com.example.projectback.user.controller;

import com.example.projectback.security.FirebaseUserPrincipal;
import com.example.projectback.user.dto.AuthMeResponse;
import com.example.projectback.user.dto.ProfileCreateRequest;
import com.example.projectback.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me(@AuthenticationPrincipal FirebaseUserPrincipal principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal));
    }

    @PostMapping("/profile")
    public ResponseEntity<AuthMeResponse> createProfile(
            @AuthenticationPrincipal FirebaseUserPrincipal principal,
            @Valid @RequestBody ProfileCreateRequest request
    ) {
        AuthMeResponse response = authService.createProfile(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
