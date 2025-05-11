package com.studia.restaurant.services;

import com.studia.restaurant.domain.ReviewCreateUpdateRequest;
import com.studia.restaurant.domain.entities.Review;
import com.studia.restaurant.domain.entities.User;

public interface ReviewService {
    Review createReview(User author, String restaurantId, ReviewCreateUpdateRequest review);

}
