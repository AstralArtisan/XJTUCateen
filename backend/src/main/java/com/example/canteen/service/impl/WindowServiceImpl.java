package com.example.canteen.service.impl;

import com.example.canteen.dto.review.ReviewResponse;
import com.example.canteen.dto.window.WindowDetailResponse;
import com.example.canteen.dto.window.WindowSummaryResponse;
import com.example.canteen.entity.CanteenWindow;
import com.example.canteen.entity.Review;
import com.example.canteen.exception.BizException;
import com.example.canteen.repository.CanteenWindowRepository;
import com.example.canteen.repository.ReviewRepository;
import com.example.canteen.service.WindowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WindowServiceImpl implements WindowService {

    private final CanteenWindowRepository canteenWindowRepository;
    private final ReviewRepository reviewRepository;

    public WindowServiceImpl(CanteenWindowRepository canteenWindowRepository, ReviewRepository reviewRepository) {
        this.canteenWindowRepository = canteenWindowRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Page<WindowSummaryResponse> searchWindows(
            String keyword,
            String canteenName,
            String cuisineType,
            int page,
            int size,
            String sort
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(safePage, safeSize, resolveSort(sort));

        return canteenWindowRepository.search(keyword, canteenName, cuisineType, pageable)
                .map(this::toSummary);
    }

    @Override
    public WindowDetailResponse getWindowDetail(Long windowId) {
        CanteenWindow window = canteenWindowRepository.findById(windowId)
                .orElseThrow(() -> new BizException(4040, "窗口不存在"));

        return new WindowDetailResponse(
                window.getId(),
                window.getCanteen().getId(),
                window.getCanteen().getName(),
                window.getWindowName(),
                window.getCuisineType(),
                window.getIntro(),
                window.getAvgScore(),
                window.getReviewCount(),
                window.getStatus()
        );
    }

    @Override
    public Page<ReviewResponse> listWindowReviews(Long windowId, int page, int size) {
        if (!canteenWindowRepository.existsById(windowId)) {
            throw new BizException(4040, "窗口不存在");
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        return reviewRepository.findByWindow_IdOrderByCreatedAtDesc(windowId, pageable)
                .map(this::toReviewResponse);
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Order.desc("avgScore"), Sort.Order.desc("reviewCount"));
        }

        return switch (sort) {
            case "hot" -> Sort.by(Sort.Order.desc("reviewCount"), Sort.Order.desc("avgScore"));
            case "latest" -> Sort.by(Sort.Order.desc("updatedAt"));
            case "name" -> Sort.by(Sort.Order.asc("windowName"));
            default -> Sort.by(Sort.Order.desc("avgScore"), Sort.Order.desc("reviewCount"));
        };
    }

    private WindowSummaryResponse toSummary(CanteenWindow window) {
        return new WindowSummaryResponse(
                window.getId(),
                window.getCanteen().getName(),
                window.getWindowName(),
                window.getCuisineType(),
                window.getIntro(),
                window.getAvgScore(),
                window.getReviewCount()
        );
    }

    private ReviewResponse toReviewResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getWindow().getId(),
                review.getWindow().getWindowName(),
                review.getWindow().getCanteen().getName(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getScore(),
                review.getCommentText(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
