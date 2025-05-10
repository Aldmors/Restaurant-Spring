package com.studia.restaurant.services;

import com.studia.restaurant.domain.RestaurantCreateUpdateRequest;
import com.studia.restaurant.domain.entities.Restaurant;

public interface RestaurantService {
    Restaurant createRestaurant(RestaurantCreateUpdateRequest request);
}
