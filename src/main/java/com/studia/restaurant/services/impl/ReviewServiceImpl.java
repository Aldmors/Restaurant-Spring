package com.studia.restaurant.services.impl;

import com.studia.restaurant.domain.ReviewCreateUpdateRequest;
import com.studia.restaurant.domain.entities.Photo;
import com.studia.restaurant.domain.entities.Restaurant;
import com.studia.restaurant.domain.entities.Review;
import com.studia.restaurant.domain.entities.User;
import com.studia.restaurant.exceptions.RestaurantNotFoundException;
import com.studia.restaurant.exceptions.ReviewNotAllowedException;
import com.studia.restaurant.repositories.RestaurantRepository;
import com.studia.restaurant.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the ReviewService interface, providing methods to manage reviews
 * for restaurants including creating, updating, deleting, retrieving, and listing reviews.
 * This service ensures validation of operations such as preventing multiple reviews
 * by the same user for a restaurant and imposing restrictions on review edits.
 * Utilizes a {@link RestaurantRepository} for database interactions.
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final RestaurantRepository restaurantRepository;

    /**
     * Retrieves a specific review from a restaurant based on the review's unique identifier.
     *
     * @param reviewId   the unique identifier of the review to retrieve
     * @param restaurant the restaurant object where the review is to be searched
     * @return an {@code Optional} containing the matching {@code Review} if found, or an empty {@code Optional} if no such review exists in the restaurant
     */
    private static Optional<Review> getReviewFromRestaurant(String reviewId, Restaurant restaurant) {
        return restaurant.getReviews()
                .stream()
                .filter(r -> reviewId.equals(r.getId()))
                .findFirst();
    }

    /**
     * Creates a new review for a specified restaurant by a given user. Validates if the user
     * already has an existing review for the restaurant. If the user has already reviewed
     * the restaurant, a {@code ReviewNotAllowedException} is thrown. The method also updates
     * the average rating of the restaurant after adding the new review.
     *
     * @param author          The user who is creating the review.
     * @param restaurantId    The unique identifier of the restaurant being reviewed.
     * @param review          The request object containing the details of the review to be created,
     *                        including content, rating, and photo URLs.
     * @return The newly created {@code Review} object.
     * @throws ReviewNotAllowedException If the user has already submitted a review for the specified restaurant.
     * @throws RuntimeException          If the review creation fails for unknown reasons.
     */
    @Override
    public Review createReview(User author, String restaurantId, ReviewCreateUpdateRequest review) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);

        boolean hasExistingReview = restaurant.getReviews().stream().anyMatch(r -> r.getWrittenBy().getId().equals(author.getId()));

        if (hasExistingReview){
            throw new ReviewNotAllowedException("User already has a review for this restaurant");
        }

        LocalDateTime now = LocalDateTime.now();

        List<Photo> photos = review.getPhotoIds().stream().map(url -> {
            return Photo.builder()
                    .url(url)
                    .uploadDate(now)
                    .build();
        }).toList();

        String reviewId = UUID.randomUUID().toString();

        Review reviewToCreate = Review .builder()
                .id(reviewId)
                .content(review.getContent())
                .rating(review.getRating())
                .photos(photos)
                .datePosted(now)
                .lastEdited(now)
                .writtenBy(author)
                .build();

        restaurant.getReviews().add(reviewToCreate);

        updateRestaurantAverageRating(restaurant);

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        return savedRestaurant.getReviews().stream()
                .filter(r -> reviewId.equals(r.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to create review"));
    }

    /**
     * Retrieves a paginated list of reviews for a specific restaurant, sorted based on the provided criteria.
     * If no sorting criteria is specified, reviews are sorted by date posted in descending order.
     *
     * @param restaurantId the unique identifier of the restaurant for which reviews are to be listed
     * @param pageable the pagination and sorting information, including page number, size, and sorting options
     * @return a page of reviews for the specified restaurant, adhering to the provided pagination and sorting options
     */
    @Override
    public Page<Review> listReviews(String restaurantId, Pageable pageable) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        List<Review> reviews = restaurant.getReviews();

        Sort sort = pageable.getSort();

        if (sort.isSorted()) {
            Sort.Order order = sort.iterator().next();
            String property = order.getProperty();
            boolean isAscending = order.getDirection().isAscending();

            Comparator<Review> comparator = switch (property) {
                case "datePosted" -> Comparator.comparing(Review::getDatePosted);
                case "rating" -> Comparator.comparing(Review::getRating);
                default -> Comparator.comparing(Review::getDatePosted);
            };

            reviews.sort(isAscending ? comparator : comparator.reversed());
        } else {
            reviews.sort(Comparator.comparing(Review::getDatePosted).reversed());
        }

        int start = (int) pageable.getOffset();

        if (start >= reviews.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, reviews.size());
        }

        int end = Math.min((start + pageable.getPageSize()), reviews.size());

        return new PageImpl<>(reviews.subList(start, end), pageable, reviews.size());
    }

    /**
     * Retrieves a specific review for a given restaurant.
     *
     * @param restaurantId the ID of the restaurant where the review is located
     * @param reviewId the ID of the review to retrieve
     * @return an {@code Optional} containing the review if found, or an empty {@code Optional} if not found
     */
    @Override
    public Optional<Review> getReview(String restaurantId, String reviewId) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        return getReviewFromRestaurant(reviewId, restaurant);
    }

    /**
     * Updates an existing review for a given restaurant.
     * Validates the user ownership and ensures the review is updated
     * within the allowed time frame. Updates the restaurant's average rating
     * after the review is modified.
     *
     * @param author the user attempting to update the review
     * @param restaurantId the unique identifier of the restaurant
     * @param reviewId the unique identifier of the review to be updated
     * @param review the updated review data provided by the user
     * @return the updated review after applying changes
     * @throws ReviewNotAllowedException if the user is not the author of the review,
     *         if the review does not exist, or if the review is being updated after
     *         the allowed time frame
     * @throws RestaurantNotFoundException if the restaurant with the specified ID does not exist
     */
    @Override
    public Review updateReview(User author, String restaurantId, String reviewId, ReviewCreateUpdateRequest review) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);

        String authorId = author.getId();
        Review existingReview = getReviewFromRestaurant(reviewId, restaurant)
                .orElseThrow(() -> new ReviewNotAllowedException("Review does not exist"));

        if (!authorId.equals(existingReview.getWrittenBy().getId())) {
            throw new ReviewNotAllowedException("Cannot update another user's review");
        }

        if (LocalDateTime.now().isAfter(existingReview.getDatePosted().plusHours(48))) {
            throw new ReviewNotAllowedException("Review can no longer bew edited");
        }

        existingReview.setContent(review.getContent());
        existingReview.setRating(review.getRating());
        existingReview.setLastEdited(LocalDateTime.now());

        existingReview.setPhotos(review.getPhotoIds().stream()
                .map(photoId -> Photo.builder()
                        .url(photoId)
                        .uploadDate(LocalDateTime.now())
                        .build()).toList());

        List<Review> updatedReviews = restaurant.getReviews().stream()
                .filter(r -> !reviewId.equals(r.getId()))
                .collect(Collectors.toList());
        updatedReviews.add(existingReview);

        restaurant.setReviews(updatedReviews);

        updateRestaurantAverageRating(restaurant);

        restaurantRepository.save(restaurant);

        return existingReview;
    }

    /**
     * Deletes a review associated with a specific restaurant.
     * The method removes the review with the given review ID from the restaurant's list of reviews,
     * updates the restaurant's average rating, and persists the changes to the database.
     *
     * @param restaurantId the ID of the restaurant from which the review is to be deleted
     * @param reviewId the ID of the review to be deleted
     */
    @Override
    public void deleteReview(String restaurantId, String reviewId) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        List<Review> filteredReviews = restaurant.getReviews().stream()
                .filter(r -> !reviewId.equals(r.getId()))
                .toList();

        restaurant.setReviews(filteredReviews);

        updateRestaurantAverageRating(restaurant);

        restaurantRepository.save(restaurant);
    }

    /**
     * Retrieves a restaurant by its ID from the repository.
     * If the restaurant is not found, throws a {@link RestaurantNotFoundException}.
     *
     * @param restaurantId the unique identifier of the restaurant to be retrieved
     * @return the {@link Restaurant} corresponding to the given ID
     * @throws RestaurantNotFoundException if no restaurant with the given ID is found
     */
    private Restaurant getRestaurantOrThrow(String restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(
                                "Restaurant with id not found: " + restaurantId
                        )
                );
    }


    /**
     * Updates the average rating of the given restaurant based on its reviews.
     * If the restaurant has no reviews, the average rating is set to 0.
     *
     * @param restaurant the restaurant whose average rating is to be updated
     */
    private void updateRestaurantAverageRating(Restaurant restaurant) {
        List<Review> reviews = restaurant.getReviews();
        if (reviews.isEmpty()) {
            restaurant.setAverageRating(0f);
        } else {
            double averageRating = reviews.stream().mapToDouble(Review::getRating)
                    .average()
                    .orElse(0.0);
            restaurant.setAverageRating((float) averageRating);
        }

    }
}
