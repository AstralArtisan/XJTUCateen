package com.example.canteen.controller;

import com.example.canteen.common.ApiResponse;
import com.example.canteen.dto.review.ReviewResponse;
import com.example.canteen.dto.review.ReviewSubmitRequest;
import com.example.canteen.service.ReviewService;
import com.example.canteen.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/windows/{windowId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ApiResponse<ReviewResponse> submitReview(
            @PathVariable Long windowId,
            @Valid @RequestBody ReviewSubmitRequest request,
            HttpSession session
    ) {
        Long userId = SessionUtil.requireLogin(session);
        return ApiResponse.success("评价提交成功", reviewService.submitReview(windowId, userId, request));
    }
}
