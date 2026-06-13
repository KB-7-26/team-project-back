package com.example.projectback.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardReportRequest {

    @NotBlank(message = "신고 사유를 입력해 주세요.")
    private String reason;
}