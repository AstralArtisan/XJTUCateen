package com.example.canteen.dto.window;

import java.math.BigDecimal;

public record WindowSummaryResponse(
        Long id,
        String canteenName,
        String windowName,
        String cuisineType,
        String intro,
        BigDecimal avgScore,
        Integer reviewCount
) {
}
