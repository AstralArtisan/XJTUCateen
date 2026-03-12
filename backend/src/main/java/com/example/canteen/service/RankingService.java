package com.example.canteen.service;

import com.example.canteen.dto.ranking.LatestReviewRankingResponse;
import com.example.canteen.dto.ranking.WindowRankingResponse;

import java.util.List;

public interface RankingService {

    List<WindowRankingResponse> topRated(int limit);

    List<WindowRankingResponse> hot(int limit);

    List<LatestReviewRankingResponse> latestReviews(int limit);
}
