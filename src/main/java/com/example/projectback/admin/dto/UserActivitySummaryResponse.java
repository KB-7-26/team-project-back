package com.example.projectback.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserActivitySummaryResponse {
    private final long productCount;
    private final long salesCount;
    private final long purchaseCount;
    private final long postCount;
    private final long commentCount;
}