package com.studia.restaurant.services;

import com.studia.restaurant.domain.ReviewCreateUpdateRequest;
import com.studia.restaurant.domain.entities.Review;
import com.studia.restaurant.domain.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReviewService {
    Review createReview(User author, String restaurantId, ReviewCreateUpdateRequest review);

    Page<Review> listReviews(String restaurantId, Pageable pageable);

    Optional<Review> getReview(String restaurantId, String reviewId);

    Review updateReview(User author, String restaurantId, String reviewId, ReviewCreateUpdateRequest review);

    void deleteReview(String restaurantId, String reviewId);
}
