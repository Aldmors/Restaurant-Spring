package com.studia.restaurant.controllers;

import com.studia.restaurant.domain.dtos.PhotoDto;
import com.studia.restaurant.domain.entities.Photo;
import com.studia.restaurant.mappers.PhotoMapper;
import com.studia.restaurant.services.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/photos")
public class PhotoController {

    private final PhotoService photoService;
    private final PhotoMapper photoMapper;

    /**
     * Handles the uploading of a photo.
     * This method accepts a file, processes it, and returns a DTO containing the details
     * of the uploaded photo.
     *
     * @param file the photo file to be uploaded
     * @return a PhotoDto object containing the details of the uploaded photo, including its URL and upload date
     */
    @PostMapping
    public PhotoDto uploadPhoto(@RequestParam("file")MultipartFile file) {
        Photo savedPhoto = photoService.uploadPhoto(file);
        return photoMapper.toDto(savedPhoto);
    }

    /**
     * Retrieves the photo resource with the specified identifier.
     * The photo is returned as a downloadable or viewable resource.
     * If the photo is not found, a 404 Not Found response is returned.
     *
     * @param id the identifier of the photo to be retrieved
     * @return a ResponseEntity containing the photo as a Resource with appropriate headers,
     *         or a 404 Not Found response if the photo does not exist
     */
    @GetMapping(path = "/{id:.+}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String id) {
        return photoService.getPhotoAsResource(id).map(photo ->
                ResponseEntity.ok()
                        .contentType(
                                MediaTypeFactory.getMediaType(photo)
                                        .orElse(MediaType.APPLICATION_OCTET_STREAM)
                        )
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                        .body(photo)
        ).orElse(ResponseEntity.notFound().build());
    }

}
