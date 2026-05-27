package com.example.projectback.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingFirebaseUserResponse {

    private String email;
    private String name;
    private String profileImageUrl;
}
