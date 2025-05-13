package com.studia.restaurant.services;

import com.studia.restaurant.domain.GeoLocation;
import com.studia.restaurant.domain.entities.Address;


public interface GeoLocationService {
    GeoLocation geoLocate(Address address);
}
