package com.example.projectback.admin.dto;

import com.example.projectback.entity.UserReport;
import com.example.projectback.report.UserReportStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AdminReportListResponse {

    private final long totalCount;
    private final long pendingCount;
    private final List<UserReportItem> reports;

    public AdminReportListResponse(long totalCount, long pendingCount, List<UserReportItem> reports) {
        this.totalCount = totalCount;
        this.pendingCount = pendingCount;
        this.reports = reports;
    }

    @Getter
    public static class UserReportItem {
        private final Long id;
        private final String reporterNickname;
        private final String reportedUserNickname;
        private final String productTitle;
        private final UserReportStatus status;
        private final LocalDateTime createdAt;

        public UserReportItem(UserReport report) {
            this.id = report.getId();
            this.reporterNickname = report.getReporter().getNickname();
            this.reportedUserNickname = report.getReportedUser().getNickname();
            this.productTitle = report.getProduct().getTitle();
            this.status = report.getStatus();
            this.createdAt = report.getCreatedAt();
        }
    }
}