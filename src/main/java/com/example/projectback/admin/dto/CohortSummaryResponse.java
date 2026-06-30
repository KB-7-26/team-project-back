package com.example.projectback.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CohortSummaryResponse {
    private final String cohort;
    private final long userCount;
}