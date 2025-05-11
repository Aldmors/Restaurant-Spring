package com.studia.restaurant.mappers;

import com.studia.restaurant.domain.ReviewCreateUpdateRequest;
import com.studia.restaurant.domain.dtos.ReviewCreateUpdateRequestDto;
import com.studia.restaurant.domain.dtos.ReviewDto;
import com.studia.restaurant.domain.entities.Review;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ReviewMapper {

    ReviewCreateUpdateRequest toReviewCreateUpdateRequest(ReviewCreateUpdateRequestDto dto);

    ReviewDto toDto(Review review);
}
