package com.example.canteen.dto.ranking;

import java.time.LocalDateTime;

public record LatestReviewRankingResponse(
        Long reviewId,
        Long windowId,
        String windowName,
        String canteenName,
        String userNickname,
        Integer score,
        String commentText,
        LocalDateTime createdAt
) {
}
