package com.studia.restaurant.controllers;

import com.studia.restaurant.domain.ReviewCreateUpdateRequest;
import com.studia.restaurant.domain.dtos.ReviewCreateUpdateRequestDto;
import com.studia.restaurant.domain.dtos.ReviewDto;
import com.studia.restaurant.domain.entities.Review;
import com.studia.restaurant.domain.entities.User;
import com.studia.restaurant.mappers.ReviewMapper;
import com.studia.restaurant.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/restaurants/{restaurantId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    /**
     * A mapper component used for converting between different representations of review-related data.
     *
     * This field is utilized throughout the controller to perform transformations such as:
     * - Converting incoming data transfer objects (DTOs) to domain-specific request objects.
     * - Mapping domain entities to data transfer objects for API responses.
     *
     * The mapper plays a key role in maintaining separation of concerns within the controller by
     * abstracting data transformation logic.
     */
    private final ReviewMapper reviewMapper;
    /**
     * Service responsible for handling operations related to reviews,
     * such as creating, listing, retrieving, updating, and deleting reviews
     * associated with restaurants.
     */
    private final ReviewService reviewService;

    /**
     * Creates a new review for a given restaurant.
     *
     * @param restaurantId the identifier of the restaurant for which the review is created
     * @param review the data transfer object containing the review content, rating, and associated photo IDs
     * @param jwt the JSON Web Token containing user authentication information
     * @return a ResponseEntity containing the newly created review in the form of a ReviewDto
     */
    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            @PathVariable String restaurantId,
            @Valid @RequestBody ReviewCreateUpdateRequestDto review,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ReviewCreateUpdateRequest reviewCreateUpdateRequest = reviewMapper.toReviewCreateUpdateRequest(review);


        User user = jwtToUser(jwt);
        Review createdReview = reviewService.createReview(user, restaurantId, reviewCreateUpdateRequest);

        return ResponseEntity.ok(reviewMapper.toDto(createdReview));
    }

    /**
     * Retrieves a paginated list of reviews for a specific restaurant, sorted by the date posted in descending order.
     *
     * @param restaurantId the unique identifier of the restaurant for which reviews need to be listed
     * @param pageable pagination and sorting information, including page size, page number,
     *                 sort field, and sort direction
     * @return a paginated list of reviews represented by {@code ReviewDto} objects
     */
    @GetMapping
    public Page<ReviewDto> listReviews(
            @PathVariable String restaurantId,
            @PageableDefault(size = 20, page = 0, sort = "datePosted", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return reviewService
                .listReviews(restaurantId, pageable)
                .map(reviewMapper::toDto);
    }

    /**
     * Retrieves a review for a specified restaurant and review ID.
     * If the review is found, it is returned as a DTO wrapped in a ResponseEntity with HTTP status 200.
     * If the review is not found, an empty ResponseEntity with HTTP status 204 is returned.
     *
     * @param restaurantId the unique identifier of the restaurant
     * @param reviewId the unique identifier of the review
     * @return ResponseEntity containing the review as a ReviewDto if found, or an empty response with HTTP status 204 if not found
     */
    @GetMapping(path = "/{reviewId}")
    public ResponseEntity<ReviewDto> getReview(
            @PathVariable String restaurantId,
            @PathVariable String reviewId
    ) {
        return reviewService.getReview(restaurantId, reviewId)
                .map(reviewMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Updates an existing review for a specific restaurant.
     *
     * @param restaurantId the unique identifier of the restaurant for which the review is being updated
     * @param reviewId the unique identifier of the review to be updated
     * @param review the review data containing updated content, rating, and optional photo references
     * @param jwt the JSON Web Token containing authenticated user information
     * @return a {@code ResponseEntity} containing the updated review as a {@code ReviewDto}
     */
    @PutMapping(path = "/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable String restaurantId,
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewCreateUpdateRequestDto review,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ReviewCreateUpdateRequest reviewCreateUpdateRequest = reviewMapper.toReviewCreateUpdateRequest(review);
        User user = jwtToUser(jwt);

        Review updatedReview = reviewService.updateReview(
                user, restaurantId, reviewId, reviewCreateUpdateRequest
        );

        return ResponseEntity.ok(reviewMapper.toDto(updatedReview));
    }

    /**
     * Deletes a specific review from a given restaurant.
     *
     * @param restaurantId the unique identifier of the restaurant
     * @param reviewId the unique identifier of the review to be deleted
     * @return a ResponseEntity with HTTP 204 No Content status if the deletion is successful
     */
    @DeleteMapping(path = "/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable String restaurantId,
            @PathVariable String reviewId
    ) {
        reviewService.deleteReview(restaurantId, reviewId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Converts a Jwt object into a User entity by extracting relevant claims.
     *
     * @param jwt the Jwt object containing the user's claims
     * @return a User object built from the claims in the Jwt
     */
    private User jwtToUser(Jwt jwt) {
        return User.builder()
                .id(jwt.getSubject())
                .username(jwt.getClaimAsString("preferred_username"))
                .givenName(jwt.getClaimAsString("given_name"))
                .familyName(jwt.getClaimAsString("family_name"))
                .build();
    }
}
