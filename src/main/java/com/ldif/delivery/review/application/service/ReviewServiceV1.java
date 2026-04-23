package com.ldif.delivery.review.application.service;

import com.ldif.delivery.review.presentation.dto.ResReviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceV1 {
    public Page<ResReviewDto> getReviews(Pageable pageable) {
    }
}
