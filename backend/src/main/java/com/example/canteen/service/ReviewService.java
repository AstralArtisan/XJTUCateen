package com.example.canteen.service;

import com.example.canteen.dto.review.ReviewResponse;
import com.example.canteen.dto.review.ReviewSubmitRequest;

public interface ReviewService {

    ReviewResponse submitReview(Long windowId, Long userId, ReviewSubmitRequest request);
}
