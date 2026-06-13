package com.example.projectback.transaction.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequest {
    private Long transactionId; // 어떤 거래에 대한 리뷰인지
    private Integer rating;     // 별점 1~5
    private String content;     // 리뷰 내용 (선택)
}
