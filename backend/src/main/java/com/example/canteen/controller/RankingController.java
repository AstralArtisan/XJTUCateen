package com.example.canteen.controller;

import com.example.canteen.common.ApiResponse;
import com.example.canteen.dto.ranking.LatestReviewRankingResponse;
import com.example.canteen.dto.ranking.WindowRankingResponse;
import com.example.canteen.service.RankingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/top-rated")
    public ApiResponse<List<WindowRankingResponse>> topRated(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(rankingService.topRated(limit));
    }

    @GetMapping("/hot")
    public ApiResponse<List<WindowRankingResponse>> hot(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(rankingService.hot(limit));
    }

    @GetMapping("/latest-reviews")
    public ApiResponse<List<LatestReviewRankingResponse>> latestReviews(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(rankingService.latestReviews(limit));
    }
}
