package com.example.canteen.dto.window;

import java.math.BigDecimal;

public record WindowDetailResponse(
        Long id,
        Long canteenId,
        String canteenName,
        String windowName,
        String cuisineType,
        String intro,
        BigDecimal avgScore,
        Integer reviewCount,
        String status
) {
}
