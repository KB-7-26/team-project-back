package com.example.projectback.admin.dto;

import com.example.projectback.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReportsByCohortResponse {

    private final String cohort;
    private final int reportedUserCount;
    private final int totalReportCount;
    private final List<ReportedUserSummary> users;

    @Getter
    public static class ReportedUserSummary {
        private final long userId;
        private final String nickname;
        private final String email;
        private final int totalReportCount;

        public ReportedUserSummary(User user, int totalReportCount) {
            this.userId = user.getId();
            this.nickname = user.getNickname();
            this.email = user.getEmail();
            this.totalReportCount = totalReportCount;
        }
    }
}