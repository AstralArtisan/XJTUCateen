package com.example.canteen.service.impl;

import com.example.canteen.dto.review.ReviewResponse;
import com.example.canteen.dto.review.ReviewSubmitRequest;
import com.example.canteen.entity.AppUser;
import com.example.canteen.entity.CanteenWindow;
import com.example.canteen.entity.Review;
import com.example.canteen.exception.BizException;
import com.example.canteen.repository.AppUserRepository;
import com.example.canteen.repository.CanteenWindowRepository;
import com.example.canteen.repository.ReviewRepository;
import com.example.canteen.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CanteenWindowRepository canteenWindowRepository;
    private final AppUserRepository appUserRepository;

    public ReviewServiceImpl(
            ReviewRepository reviewRepository,
            CanteenWindowRepository canteenWindowRepository,
            AppUserRepository appUserRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.canteenWindowRepository = canteenWindowRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional
    public ReviewResponse submitReview(Long windowId, Long userId, ReviewSubmitRequest request) {
        CanteenWindow window = canteenWindowRepository.findById(windowId)
                .orElseThrow(() -> new BizException(4040, "窗口不存在"));

        if (!"OPEN".equalsIgnoreCase(window.getStatus())) {
            throw new BizException(4004, "窗口暂不可评价");
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new BizException(4012, "用户不存在或登录已失效"));

        Review review = reviewRepository.findByUser_IdAndWindow_Id(userId, windowId).orElseGet(() -> {
            Review created = new Review();
            created.setUser(user);
            created.setWindow(window);
            return created;
        });

        review.setScore(request.getScore());
        review.setCommentText(request.getCommentText().trim());

        Review saved = reviewRepository.save(review);
        updateWindowStats(window);

        return new ReviewResponse(
                saved.getId(),
                saved.getWindow().getId(),
                saved.getWindow().getWindowName(),
                saved.getWindow().getCanteen().getName(),
                saved.getUser().getId(),
                saved.getUser().getNickname(),
                saved.getScore(),
                saved.getCommentText(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    private void updateWindowStats(CanteenWindow window) {
        Double avg = reviewRepository.findAvgScoreByWindowId(window.getId());
        long count = reviewRepository.countByWindow_Id(window.getId());
        BigDecimal avgScore = toDecimal(avg);
        int reviewCount = Math.toIntExact(count);

        window.setAvgScore(avgScore.setScale(2, RoundingMode.HALF_UP));
        window.setReviewCount(reviewCount);
        canteenWindowRepository.save(window);
    }

    private BigDecimal toDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }
}
