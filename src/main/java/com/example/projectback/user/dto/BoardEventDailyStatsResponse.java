package com.example.projectback.user.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class BoardEventDailyStatsResponse {

    private final LocalDate date;
    private final int day;
    private final long postCount;
    private final long commentCount;
    private final long totalCount;

    public BoardEventDailyStatsResponse(LocalDate date, long postCount, long commentCount) {
        this.date = date;
        this.day = date.getDayOfMonth();
        this.postCount = postCount;
        this.commentCount = commentCount;
        this.totalCount = postCount + commentCount;
    }
}
