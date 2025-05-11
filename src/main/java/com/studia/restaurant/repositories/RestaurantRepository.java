package com.studia.restaurant.repositories;

import com.studia.restaurant.domain.entities.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends ElasticsearchRepository<Restaurant, String> {
    Page<Restaurant> findByAverageRatingGreaterThanEqual(Float minRating, Pageable pageable);

//    TODO: Copy from example YT 3:08:07
    @Query("{}")
    Page<Restaurant> findByQueryAndMinRating(String query, Float minRating, Pageable pageable);
    //    TODO: Copy from example YT 3:08:07
    @Query("{}")
    Page<Restaurant> findByLocationNear(Float latitude, Float longitude, Float radius, Pageable pageable);
}
