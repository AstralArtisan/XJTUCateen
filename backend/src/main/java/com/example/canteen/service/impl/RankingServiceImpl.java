package com.example.canteen.service.impl;

import com.example.canteen.dto.ranking.LatestReviewRankingResponse;
import com.example.canteen.dto.ranking.WindowRankingResponse;
import com.example.canteen.entity.CanteenWindow;
import com.example.canteen.entity.Review;
import com.example.canteen.repository.CanteenWindowRepository;
import com.example.canteen.repository.ReviewRepository;
import com.example.canteen.service.RankingService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RankingServiceImpl implements RankingService {

    private final CanteenWindowRepository canteenWindowRepository;
    private final ReviewRepository reviewRepository;

    public RankingServiceImpl(CanteenWindowRepository canteenWindowRepository, ReviewRepository reviewRepository) {
        this.canteenWindowRepository = canteenWindowRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public List<WindowRankingResponse> topRated(int limit) {
        int safeLimit = normalizeLimit(limit);
        Pageable pageable = PageRequest.of(0, safeLimit);
        return canteenWindowRepository.findByStatusOrderByAvgScoreDescReviewCountDesc("OPEN", pageable)
                .stream()
                .map(this::toWindowRanking)
                .toList();
    }

    @Override
    public List<WindowRankingResponse> hot(int limit) {
        int safeLimit = normalizeLimit(limit);
        Pageable pageable = PageRequest.of(0, safeLimit);
        return canteenWindowRepository.findByStatusOrderByReviewCountDescAvgScoreDesc("OPEN", pageable)
                .stream()
                .map(this::toWindowRanking)
                .toList();
    }

    @Override
    public List<LatestReviewRankingResponse> latestReviews(int limit) {
        int safeLimit = normalizeLimit(limit);
        Pageable pageable = PageRequest.of(0, safeLimit);
        return reviewRepository.findLatest(pageable)
                .stream()
                .map(this::toLatestReviewRanking)
                .toList();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 10;
        }
        return Math.min(limit, 50);
    }

    private WindowRankingResponse toWindowRanking(CanteenWindow window) {
        return new WindowRankingResponse(
                window.getId(),
                window.getCanteen().getName(),
                window.getWindowName(),
                window.getCuisineType(),
                window.getAvgScore(),
                window.getReviewCount()
        );
    }

    private LatestReviewRankingResponse toLatestReviewRanking(Review review) {
        return new LatestReviewRankingResponse(
                review.getId(),
                review.getWindow().getId(),
                review.getWindow().getWindowName(),
                review.getWindow().getCanteen().getName(),
                review.getUser().getNickname(),
                review.getScore(),
                review.getCommentText(),
                review.getCreatedAt()
        );
    }
}
