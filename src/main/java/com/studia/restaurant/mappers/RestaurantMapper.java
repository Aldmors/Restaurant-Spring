package com.studia.restaurant.mappers;

import com.studia.restaurant.domain.RestaurantCreateUpdateRequest;
import com.studia.restaurant.domain.dtos.GeoPointDto;
import com.studia.restaurant.domain.dtos.RestaurantCreateUpdateRequestDto;
import com.studia.restaurant.domain.dtos.RestaurantDto;
import com.studia.restaurant.domain.dtos.RestaurantSummaryDto;
import com.studia.restaurant.domain.entities.Restaurant;
import com.studia.restaurant.domain.entities.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RestaurantMapper {

    /**
     * Maps a RestaurantCreateUpdateRequestDto object to a RestaurantCreateUpdateRequest object.
     *
     * @param dto the RestaurantCreateUpdateRequestDto containing the source data
     * @return the mapped RestaurantCreateUpdateRequest
     */
    RestaurantCreateUpdateRequest toRestaurantCreateUpdateRequest(RestaurantCreateUpdateRequestDto dto);

    /**
     * Maps a {@link Restaurant} entity to a {@link RestaurantDto}.
     * The mapping includes transforming the "reviews" field into the "totalReviews"
     * property using a custom mapping qualified by the name "populateTotalReviews".
     *
     * @param restaurant the {@link Restaurant} entity to be mapped
     * @return the corresponding {@link RestaurantDto} representation
     */
    @Mapping(source = "reviews", target = "totalReviews", qualifiedByName = "populateTotalReviews")
    RestaurantDto toRestaurantDto(Restaurant restaurant);

    /**
     * Converts a Restaurant entity into a RestaurantSummaryDto object.
     *
     * @param restaurant the Restaurant entity to convert
     * @return the corresponding RestaurantSummaryDto
     */
    @Mapping(source = "reviews", target = "totalReviews", qualifiedByName = "populateTotalReviews")
    RestaurantSummaryDto toSummaryDto(Restaurant restaurant);

    /**
     * Calculates the total number of reviews in the provided list.
     *
     * @param reviews the list of reviews from which the total count will be derived
     * @return the total number of reviews present in the input list
     */
    @Named("populateTotalReviews")
    default Integer populateTotalReviews(List<Review> reviews){
        return reviews.size();
    }

    /**
     * Converts a GeoPoint entity to a GeoPointDto object.
     *
     * @param geoPoint the GeoPoint entity from the domain model containing geographic information
     * @return a GeoPointDto object containing latitude and longitude, or null if geoPoint is null
     */
    @Mapping(target = "latitude", expression = "java(geoPoint.getLat())")
    @Mapping(target = "longitude", expression = "java(geoPoint.getLon())")
    GeoPointDto toGeoPointDto(GeoPoint geoPoint);
}
