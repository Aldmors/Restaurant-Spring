package com.studia.restaurant.mappers;

import com.studia.restaurant.domain.dtos.PhotoDto;
import com.studia.restaurant.domain.entities.Photo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface PhotoMapper {
    PhotoDto toDto (Photo photo);


}
