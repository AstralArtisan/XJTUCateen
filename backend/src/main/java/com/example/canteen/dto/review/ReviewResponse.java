package com.example.canteen.dto.review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long windowId,
        String windowName,
        String canteenName,
        Long userId,
        String userNickname,
        Integer score,
        String commentText,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
