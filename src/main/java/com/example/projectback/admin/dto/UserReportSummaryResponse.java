package com.example.projectback.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserReportSummaryResponse {

    private final int product;
    private final int post;
    private final int comment;
    private final int user;
}