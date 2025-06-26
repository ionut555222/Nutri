package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
public class ImageUploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Autowired
    private FruitRepository fruitRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("error", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file size
            if (file.getSize() > MAX_FILE_SIZE) {
                response.put("error", "File size must be less than 5MB");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !isValidImageFile(originalFilename)) {
                response.put("error", "Only image files (JPG, JPEG, PNG, GIF, WebP) are allowed");
                return ResponseEntity.badRequest().body(response);
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Generate URL
            String imageUrl = "/api/images/view/" + uniqueFilename;

            response.put("filename", uniqueFilename);
            response.put("url", imageUrl);
            response.put("originalName", originalFilename);
            response.put("size", String.valueOf(file.getSize()));

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/upload/fruit/{fruitId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Map<String, String>> uploadFruitImage(
            @PathVariable Long fruitId,
            @RequestParam("file") MultipartFile file) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            // Check if fruit exists
            Fruit fruit = fruitRepository.findById(fruitId)
                    .orElseThrow(() -> new IllegalArgumentException("Fruit not found"));

            // Upload image
            ResponseEntity<Map<String, String>> uploadResponse = uploadImage(file);
            if (!uploadResponse.getStatusCode().is2xxSuccessful()) {
                return uploadResponse;
            }

            Map<String, String> uploadData = uploadResponse.getBody();
            String filename = uploadData.get("filename");
            String imageUrl = uploadData.get("url");

            // Delete old image if exists
            if (fruit.getImageFilename() != null) {
                deleteImageFile(fruit.getImageFilename());
            }

            // Update fruit with image info
            fruit.setImageFilename(filename);
            fruit.setImageUrl(imageUrl);
            fruitRepository.save(fruit);

            response.put("message", "Image uploaded successfully");
            response.put("filename", filename);
            response.put("url", imageUrl);
            response.put("fruitId", fruitId.toString());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/view/{filename}")
    public ResponseEntity<Resource> viewImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = getContentType(filename);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete/{filename}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable String filename) {
        Map<String, String> response = new HashMap<>();
        
        try {
            boolean deleted = deleteImageFile(filename);
            if (deleted) {
                response.put("message", "Image deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Image not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("error", "Failed to delete image: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private boolean isValidImageFile(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        for (String extension : ALLOWED_EXTENSIONS) {
            if (lowerCaseFilename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex != -1) ? filename.substring(lastDotIndex) : "";
    }

    private String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }

    private boolean deleteImageFile(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
} 