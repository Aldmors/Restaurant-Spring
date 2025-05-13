package com.studia.restaurant.controllers;

import com.studia.restaurant.domain.RestaurantCreateUpdateRequest;
import com.studia.restaurant.domain.dtos.RestaurantCreateUpdateRequestDto;
import com.studia.restaurant.domain.dtos.RestaurantDto;
import com.studia.restaurant.domain.dtos.RestaurantSummaryDto;
import com.studia.restaurant.domain.entities.Restaurant;
import com.studia.restaurant.mappers.RestaurantMapper;
import com.studia.restaurant.services.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Controller responsible for handling restaurant-related API endpoints.
 */
@RestController
@RequestMapping(path = "/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final RestaurantMapper restaurantMapper;

    /**
     * Creates a new restaurant based on the provided request data.
     *
     * @param request the details of the restaurant to be created, encapsulated in a
     *                {@link RestaurantCreateUpdateRequestDto} object
     * @return a {@link ResponseEntity} containing the created restaurant details
     *         as a {@link RestaurantDto}
     */
    @PostMapping
    public ResponseEntity<RestaurantDto> createRestaurant(
            @Valid @RequestBody RestaurantCreateUpdateRequestDto request
    ) {
        RestaurantCreateUpdateRequest restaurantCreateUpdateRequest = restaurantMapper
                .toRestaurantCreateUpdateRequest(request);

        Restaurant restaurant = restaurantService.createRestaurant(restaurantCreateUpdateRequest);
        RestaurantDto createdRestaurantDto = restaurantMapper.toRestaurantDto(restaurant);
        return ResponseEntity.ok(createdRestaurantDto);
    }

    /**
     * Searches for restaurants based on various filters including query, minimum rating, geographical coordinates,
     * and radius, and returns a paginated list of restaurant summaries.
     *
     * @param q          an optional search term to filter restaurants by name, cuisine type, or other attributes
     * @param minRating  an optional minimum average rating to filter restaurants
     * @param latitude   an optional latitude for geographical filtering
     * @param longitude  an optional longitude for geographical filtering
     * @param radius     an optional radius in which to search for restaurants, centered around the provided latitude and longitude
     * @param page       the page number to retrieve, starting from 1; defaults to 1 if not specified
     * @param size       the number of restaurants to include in each page; defaults to 20 if not specified
     * @return a paginated list of RestaurantSummaryDto objects representing matching restaurants
     */
    @GetMapping
    public Page<RestaurantSummaryDto> searchRestaurants(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Float minRating,
            @RequestParam(required = false) Float latitude,
            @RequestParam(required = false) Float longitude,
            @RequestParam(required = false) Float radius,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        Page<Restaurant> searchResults = restaurantService.searchRestaurants(
                q, minRating, latitude, longitude, radius, PageRequest.of(page - 1, size)
        );

        return searchResults.map(restaurantMapper::toSummaryDto);
    }

    /**
     * Retrieves the details of a restaurant by its unique identifier.
     *
     * @param restaurantId the unique identifier of the restaurant to be retrieved
     * @return a {@link ResponseEntity} containing the {@link RestaurantDto} if the restaurant exists;
     *         otherwise, a {@link ResponseEntity} with a 404 Not Found status
     */
    @GetMapping(path = "/{restaurant_id}")
    public ResponseEntity<RestaurantDto> getRestaurant(@PathVariable("restaurant_id") String restaurantId) {
        return restaurantService.getRestaurant(restaurantId)
                .map(restaurant -> ResponseEntity.ok(restaurantMapper.toRestaurantDto(restaurant)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing restaurant's information based on the provided request data.
     *
     * @param restaurantId the unique identifier of the restaurant to be updated
     * @param requestDto the request body containing the updated restaurant details
     * @return a ResponseEntity containing the updated restaurant details as a RestaurantDto
     */
    @PutMapping(path = "/{restaurant_id}")
    public ResponseEntity<RestaurantDto> updateRestaurant(
            @PathVariable("restaurant_id") String restaurantId,
            @Valid @RequestBody RestaurantCreateUpdateRequestDto requestDto
    ) {
        RestaurantCreateUpdateRequest request = restaurantMapper
                .toRestaurantCreateUpdateRequest(requestDto);

        Restaurant updatedRestaurant = restaurantService.updateRestaurant(restaurantId, request);

        return ResponseEntity.ok(restaurantMapper.toRestaurantDto(updatedRestaurant));
    }

    /**
     * Deletes a restaurant by its unique identifier.
     *
     * @param restaurantId the unique identifier of the restaurant to be deleted
     * @return a {@code ResponseEntity} with no content if the deletion is successful
     */
    @DeleteMapping(path = "/{restaurant_id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable("restaurant_id") String restaurantId) {
        restaurantService.deleteRestaurant(restaurantId);
        return ResponseEntity.noContent().build();
    }



}

