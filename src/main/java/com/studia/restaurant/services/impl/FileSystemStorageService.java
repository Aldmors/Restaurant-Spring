package com.studia.restaurant.services.impl;

import com.studia.restaurant.exceptions.StorageException;
import com.studia.restaurant.services.StorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
@Slf4j
public class FileSystemStorageService implements StorageService {

    /**
     * The directory location for storing files, typically used in file
     * system storage operations. The value is injected from the
     * application properties and defaults to "uploads" if not explicitly set.
     * This variable is utilized to define the root path for file storage.
     */
    @Value("${app.storage.location:uploads}")
    private String storageLocation;

    /**
     * Represents the root directory path for file storage operations.
     * This variable is initialized based on the storage location specified
     * in the application configuration.
     * It serves as the base location for storing, retrieving, and managing files.
     */
    private Path rootLocation;

    /**
     * Initializes the storage service by creating the root directory for file storage.
     * The root location is determined using the configured `storageLocation` value.
     * If the directory already exists, no action is taken. Otherwise, the directory
     * is created. If an error occurs during directory creation, a {@link StorageException}
     * is thrown.
     *
     * @throws StorageException if the storage directory cannot be created
     */
    @PostConstruct
    public void init() {
        rootLocation = Paths.get(storageLocation);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage location", e);
        }
    }

    /**
     * Stores a given file in the specified storage location with the provided filename.
     * The file is validated to ensure it is not empty and is stored safely within the intended directory.
     *
     * @param file the {@link MultipartFile} to be stored
     * @param filename the name to use when storing the file, excluding the file extension
     * @return the final name of the stored file, including its file extension
     * @throws StorageException if the file is empty, the storage operation fails,
     * or if the file is attempted to be stored outside the specified directory
     */
    @Override
    public String store(MultipartFile file, String filename) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Cannot save an empty file");
            }

            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String finalFileName = filename + "." + extension;

            Path destinationFile = rootLocation
                    .resolve(Paths.get(finalFileName))
                    .normalize()
                    .toAbsolutePath();

            if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
                throw new StorageException("Cannot store file outside specified directory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return finalFileName;

        } catch(IOException e) {
            throw new StorageException("Failed to store file", e);
        }

    }

    /**
     * Loads a file as a {@link Resource} by its filename.
     * This method resolves the filename against a predefined root storage location and attempts to
     * create a {@link UrlResource} pointing to the resolved file. If the file does not exist
     * or is not readable, an empty {@link Optional} is returned.
     *
     * @param filename the name of the file to be loaded as a resource
     * @return an {@link Optional} containing the {@link Resource} if the file exists and is readable,
     *         or an empty {@link Optional} if the file cannot be found or accessed
     */
    @Override
    public Optional<Resource> loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename);

            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return Optional.of(resource);
            } else {
                return Optional.empty();
            }
        } catch(MalformedURLException e) {
            log.warn("Could not read file: %s".formatted(filename), e);
            return Optional.empty();
        }
    }

}
