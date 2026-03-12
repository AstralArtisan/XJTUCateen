package com.example.canteen.controller;

import com.example.canteen.common.ApiResponse;
import com.example.canteen.dto.review.ReviewResponse;
import com.example.canteen.dto.window.WindowDetailResponse;
import com.example.canteen.dto.window.WindowSummaryResponse;
import com.example.canteen.service.WindowService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/windows")
public class WindowController {

    private final WindowService windowService;

    public WindowController(WindowService windowService) {
        this.windowService = windowService;
    }

    @GetMapping
    public ApiResponse<Page<WindowSummaryResponse>> listWindows(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String canteenName,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "score") String sort
    ) {
        return ApiResponse.success(windowService.searchWindows(keyword, canteenName, cuisineType, page, size, sort));
    }

    @GetMapping("/{id}")
    public ApiResponse<WindowDetailResponse> getWindowDetail(@PathVariable("id") Long windowId) {
        return ApiResponse.success(windowService.getWindowDetail(windowId));
    }

    @GetMapping("/{id}/reviews")
    public ApiResponse<Page<ReviewResponse>> getWindowReviews(
            @PathVariable("id") Long windowId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(windowService.listWindowReviews(windowId, page, size));
    }
}
