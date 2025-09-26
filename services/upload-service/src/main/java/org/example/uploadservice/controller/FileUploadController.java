package org.example.uploadservice.controller;

import org.example.uploadservice.service.FileUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    // Upload 1 file local
    @PostMapping("/local")
    public ResponseEntity<?> uploadLocal(@RequestParam("file") MultipartFile file) {
        try {
            String result = fileUploadService.uploadFileLocal(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    // Upload nhiều file local
    @PostMapping("/local/multiple")
    public ResponseEntity<?> uploadMultipleLocal(@RequestParam("files") List<MultipartFile> files) {
        try {
            List<String> results = fileUploadService.uploadMultipleFilesLocal(files);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    // Upload 1 file cloudinary
    @PostMapping("/cloudinary")
    public ResponseEntity<?> uploadCloudinary(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileUploadService.uploadFileCloudinary(file);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    // Upload nhiều file cloudinary
    @PostMapping("/cloudinary/multiple")
    public ResponseEntity<?> uploadMultipleCloudinary(@RequestParam("files") List<MultipartFile> files) {
        try {
            List<String> urls = fileUploadService.uploadMultipleFilesCloudinary(files);
            return ResponseEntity.ok(urls);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }
}
