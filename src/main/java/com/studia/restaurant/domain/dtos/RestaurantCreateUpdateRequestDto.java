package com.studia.restaurant.domain.dtos;

import com.studia.restaurant.domain.entities.Address;
import com.studia.restaurant.domain.entities.OperatingHours;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestaurantCreateUpdateRequestDto {


    @NotBlank(message = "Restaurant name cannot be blank")
    private String name;
    @NotBlank(message = "Cuisine type cannot be blank")
    private String cuisineType;

    @Valid
    private AddressDto address;

    @NotBlank(message = "Contact information cannot be blank")
    private String contactInformation;

    @Valid
    private OperatingHoursDto operatingHours;
    @Size(min = 1, message = "At least one photo ID is required")
    private List<String> photoItds;
}
