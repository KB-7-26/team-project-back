package com.example.projectback.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileStatsResponse {

    private long activeProductCount;
    private long soldProductCount;
    private long favoriteCount;
}
