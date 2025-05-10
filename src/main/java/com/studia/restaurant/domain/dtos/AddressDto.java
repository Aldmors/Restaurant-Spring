package com.studia.restaurant.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDto {

    @NotBlank(message = "Street number cannot be blank")
    @Pattern(regexp = "^[0-9]{1,5}[a-zA-Z]?$", message = "Street number must be a number")
    private String streetNumber;

    @NotBlank(message = "Street name cannot be blank")
    private String streetName;

    private String unit;
    @NotBlank(message = "City cannot be blank")
    private String city;
    @NotBlank(message = "State cannot be blank")
    private String state;
    @NotBlank(message = "Postal code cannot be blank")
    private String postalCode;
    @NotBlank(message = "Country cannot be blank")
    private String country;
}
