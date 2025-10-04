package org.example.uploadservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.uploadservice.service.FileUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/upload")
@Tag(name = "Upload API", description = "Endpoints for uploading files to local or cloud storage")
public class FileUploadController {

  private final FileUploadService fileUploadService;

  public FileUploadController(FileUploadService fileUploadService) {
    this.fileUploadService = fileUploadService;
  }

  // -------------------- Upload 1 file local --------------------
  @Operation(summary = "Upload single file (local)", description = "Upload one file and store it locally on the server.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "File uploaded successfully",
      content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(responseCode = "400", description = "Upload failed")
  })
  @PostMapping(value = "/local", consumes = "multipart/form-data")
  public ResponseEntity<?> uploadLocal(
    @Parameter(description = "File to upload", required = true)
    @RequestParam("file") MultipartFile file
  ) {
    try {
      String result = fileUploadService.uploadFileLocal(file);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
    }
  }

  // -------------------- Upload nhiều file local --------------------
  @Operation(summary = "Upload multiple files (local)", description = "Upload multiple files and store them locally on the server.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Files uploaded successfully",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
    @ApiResponse(responseCode = "400", description = "Upload failed")
  })
  @PostMapping(value = "/local/multiple", consumes = "multipart/form-data")
  public ResponseEntity<?> uploadMultipleLocal(
    @Parameter(description = "Files to upload", required = true)
    @RequestParam("files") List<MultipartFile> files
  ) {
    try {
      List<String> results = fileUploadService.uploadMultipleFilesLocal(files);
      return ResponseEntity.ok(results);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
    }
  }

  // -------------------- Upload 1 file Cloudinary --------------------
  @Operation(summary = "Upload single file (Cloudinary)", description = "Upload one file and store it on Cloudinary.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "File uploaded successfully",
      content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(responseCode = "400", description = "Upload failed")
  })
  @PostMapping(value = "/cloudinary", consumes = "multipart/form-data")
  public ResponseEntity<?> uploadCloudinary(
    @Parameter(description = "File to upload", required = true)
    @RequestParam("file") MultipartFile file
  ) {
    try {
      String url = fileUploadService.uploadFileCloudinary(file);
      return ResponseEntity.ok(url);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
    }
  }

  // -------------------- Upload nhiều file Cloudinary --------------------
  @Operation(summary = "Upload multiple files (Cloudinary)", description = "Upload multiple files and store them on Cloudinary.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Files uploaded successfully",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
    @ApiResponse(responseCode = "400", description = "Upload failed")
  })
  @PostMapping(value = "/cloudinary/multiple", consumes = "multipart/form-data")
  public ResponseEntity<?> uploadMultipleCloudinary(
    @Parameter(description = "Files to upload", required = true)
    @RequestParam("files") List<MultipartFile> files
  ) {
    try {
      List<String> urls = fileUploadService.uploadMultipleFilesCloudinary(files);
      return ResponseEntity.ok(urls);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
    }
  }
}
