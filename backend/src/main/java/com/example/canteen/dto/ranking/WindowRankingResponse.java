package com.example.canteen.dto.ranking;

import java.math.BigDecimal;

public record WindowRankingResponse(
        Long windowId,
        String canteenName,
        String windowName,
        String cuisineType,
        BigDecimal avgScore,
        Integer reviewCount
) {
}
