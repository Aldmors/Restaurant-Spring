package com.studia.restaurant.services.impl;

import com.studia.restaurant.domain.GeoLocation;
import com.studia.restaurant.domain.RestaurantCreateUpdateRequest;
import com.studia.restaurant.domain.entities.Address;
import com.studia.restaurant.domain.entities.Photo;
import com.studia.restaurant.domain.entities.Restaurant;
import com.studia.restaurant.exceptions.RestaurantNotFoundException;
import com.studia.restaurant.repositories.RestaurantRepository;
import com.studia.restaurant.services.GeoLocationService;
import com.studia.restaurant.services.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * The RestaurantServiceImpl is a service implementation class that manages the
 * operations related to the Restaurant entity. It provides functionality for
 * creating, searching, fetching, updating, and deleting restaurant records.
 *
 * This class coordinates with the RestaurantRepository for persistence operations
 * and GeoLocationService to handle geolocation-related features.
 */
@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final GeoLocationService geoLocationService;

    /**
     * Creates a new restaurant using the provided details in the request object and saves it to the repository.
     * The method converts the supplied information such as address, photos, and other attributes
     * into a Restaurant entity, calculates the geolocation, and persists the restaurant.
     *
     * @param request an object containing the necessary details to create a restaurant, including name,
     *                cuisine type, contact information, address, operating hours, and photo IDs.
     * @return the created Restaurant entity after being saved in the repository.
     */
    @Override
    public Restaurant createRestaurant(RestaurantCreateUpdateRequest request) {
        Address address = request.getAddress();
        GeoLocation geoLocation = geoLocationService.geoLocate(address);
        GeoPoint geoPoint = new GeoPoint(geoLocation.getLatitude(), geoLocation.getLongitude());

        List<String> photoIds = request.getPhotoIds();
        List<Photo> photos = photoIds.stream().map(photoUrl -> Photo.builder()
                .url(photoUrl)
                .uploadDate(LocalDateTime.now())
                .build()).toList();

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .cuisineType(request.getCuisineType())
                .contactInformation(request.getContactInformation())
                .address(address)
                .geoLocation(geoPoint)
                .operatingHours(request.getOperatingHours())
                .averageRating(0f)
                .photos(photos)
                .build();

        return restaurantRepository.save(restaurant);
    }

    /**
     * Searches for restaurants based on the provided query parameters, such as name or cuisine type,
     * minimum rating, geographic location, and search radius. If no parameters are provided, all
     * restaurants are returned paginated. The method dynamically adapts behavior based on which parameters
     * are non-null or provided.
     *
     * @param query       the search query to match restaurant names or cuisine types; can be null or empty.
     * @param minRating   the minimum average rating of restaurants to include in the results; can be null.
     * @param latitude    the latitude for geographic filtering; used in combination with longitude and radius; can be null.
     * @param longitude   the longitude for geographic filtering; used in combination with latitude and radius; can be null.
     * @param radius      the radius in kilometers within which to search around the latitude and longitude; can be null.
     * @param pageable    the pagination information, including page number and size.
     * @return a paginated list of restaurants that match the specified search criteria.
     */
    @Override
    public Page<Restaurant> searchRestaurants(String query, Float minRating, Float latitude, Float longitude, Float radius, Pageable pageable) {
        if(null != minRating && null == query || query.isEmpty()) {
            return restaurantRepository.findByAverageRatingGreaterThanEqual(minRating, pageable);
        }

        Float searchMinRating = null == minRating ? 0f : minRating;

        if(!query.trim().isEmpty()) {
            return restaurantRepository.findByQueryAndMinRating(query, searchMinRating, pageable);
        }

        if(null != latitude || null != radius || null != longitude) {
            return restaurantRepository.findByLocationNear(latitude, longitude, radius, pageable);
        }

        return restaurantRepository.findAll(pageable);

    }

    /**
     * Retrieves a restaurant based on its unique identifier.
     *
     * @param id the unique identifier of the restaurant to fetch.
     * @return an Optional containing the restaurant if found, or an empty Optional if no restaurant exists with the given id.
     */
    @Override
    public Optional<Restaurant> getRestaurant(String id) {
        return restaurantRepository.findById(id);
    }

    /**
     * Updates an existing restaurant with new details provided in the request object.
     * The method retrieves the restaurant by its ID, updates its attributes such as name,
     * address, contact information, operating hours, and photos, recalculates the geolocation
     * based on the new address, and then saves the updated restaurant in the repository.
     *
     * @param id      the unique identifier of the restaurant to be updated.
     * @param request an object containing the updated details for the restaurant,
     *                including name, cuisine type, contact information, address,
     *                operating hours, and photo IDs.
     * @return the updated Restaurant entity after being saved in the repository.
     * @throws RestaurantNotFoundException if no restaurant exists with the given ID.
     */
    @Override
    public Restaurant updateRestaurant(String id, RestaurantCreateUpdateRequest request) {
        Restaurant restaurant = getRestaurant(id)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found: "+id));
        GeoLocation newGeoLocation = geoLocationService.geoLocate(
                request.getAddress()
        );
        GeoPoint newGeoPoint = new GeoPoint(newGeoLocation.getLatitude(), newGeoLocation.getLongitude());

        List<String> photoIds = request.getPhotoIds();
        List<Photo> photos = photoIds.stream().map(photoUrl -> Photo.builder()
                .url(photoUrl)
                .uploadDate(LocalDateTime.now())
                .build()).toList();

        restaurant.setName(request.getName());
        restaurant.setCuisineType(request.getCuisineType());
        restaurant.setContactInformation(request.getContactInformation());
        restaurant.setAddress(request.getAddress());
        restaurant.setGeoLocation(newGeoPoint);
        restaurant.setOperatingHours(request.getOperatingHours());
        restaurant.setPhotos(photos);

        return restaurantRepository.save(restaurant);

    }

    /**
     * Deletes a restaurant record by its unique identifier.
     *
     * @param id the unique identifier of the restaurant to be deleted
     */
    @Override
    public void deleteRestaurant(String id) {
        restaurantRepository.deleteById(id);
    }

}
