package com.example.canteen.service;

import com.example.canteen.dto.review.ReviewResponse;
import com.example.canteen.dto.window.WindowDetailResponse;
import com.example.canteen.dto.window.WindowSummaryResponse;
import org.springframework.data.domain.Page;

public interface WindowService {

    Page<WindowSummaryResponse> searchWindows(
            String keyword,
            String canteenName,
            String cuisineType,
            int page,
            int size,
            String sort
    );

    WindowDetailResponse getWindowDetail(Long windowId);

    Page<ReviewResponse> listWindowReviews(Long windowId, int page, int size);
}
