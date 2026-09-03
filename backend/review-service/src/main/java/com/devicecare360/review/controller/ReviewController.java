package com.devicecare360.review.controller;

import com.devicecare360.review.document.Review;
import com.devicecare360.review.service.ReviewService;
import com.devicecare360.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> createReview(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Review review) {
        Review created = service.createReview(userId, review);
        return ResponseEntity.ok(ApiResponse.success(created, "Review submitted successfully"));
    }

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<ApiResponse<List<Review>>> getReviewsByTechnician(@PathVariable String technicianId) {
        return ResponseEntity.ok(ApiResponse.success(service.getReviewsByTechnician(technicianId)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Review>>> getReviewsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(service.getReviewsByUser(userId)));
    }
}
