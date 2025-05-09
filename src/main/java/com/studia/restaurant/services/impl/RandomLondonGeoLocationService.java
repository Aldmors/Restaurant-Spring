package com.studia.restaurant.services.impl;

import com.studia.restaurant.domain.GeoLocation;
import com.studia.restaurant.domain.entities.Address;
import com.studia.restaurant.services.GeoLocationService;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RandomLondonGeoLocationService implements GeoLocationService {

    private static final double MIN_LATITUDE = 51.28f;
    private static final double MAX_LATITUDE= 51.686f;
    private  static final double MIN_LONGITUDE = -0.489f;
    private static final double MAX_LONGITUDE = 0.236f;

    @Override
    public GeoLocation geoLocate(Address address) {
        Random random = new Random();
        double latitude = random.nextDouble() * (MAX_LATITUDE - MIN_LATITUDE) + MIN_LATITUDE;
        double longitude = random.nextDouble() * (MAX_LONGITUDE - MIN_LONGITUDE) + MIN_LONGITUDE;

        return GeoLocation.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
