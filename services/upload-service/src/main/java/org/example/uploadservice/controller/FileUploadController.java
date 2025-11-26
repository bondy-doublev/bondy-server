package org.example.uploadservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.example.uploadservice.service.FileUploadService;
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
  public AppApiResponse uploadLocal(
    @Parameter(description = "File to upload", required = true)
    @RequestParam("file") MultipartFile file
  ) {
    try {
      String result = fileUploadService.uploadFileLocal(file);
      return new AppApiResponse(result);
    } catch (Exception e) {
      throw new AppException(ErrorCode.IO_ERROR, e.getMessage());
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
  public AppApiResponse uploadMultipleLocal(
    @Parameter(description = "Files to upload", required = true)
    @RequestParam("files") List<MultipartFile> files
  ) {
    try {
      List<String> results = fileUploadService.uploadMultipleFilesLocal(files);
      return new AppApiResponse(results);
    } catch (Exception e) {
      throw new AppException(ErrorCode.IO_ERROR, e.getMessage());
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
  public AppApiResponse uploadCloudinary(
    @Parameter(description = "File to upload", required = true)
    @RequestParam("file") MultipartFile file
  ) {
    try {
      String url = fileUploadService.uploadFileCloudinary(file);
      return new AppApiResponse(url);
    } catch (Exception e) {
      throw new AppException(ErrorCode.IO_ERROR, e.getMessage());
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
  public AppApiResponse uploadMultipleCloudinary(
    @Parameter(description = "Files to upload", required = true)
    @RequestParam("files") List<MultipartFile> files
  ) {
    try {
      List<String> urls = fileUploadService.uploadMultipleFilesCloudinary(files);
      return new AppApiResponse(urls);
    } catch (Exception e) {
      throw new AppException(ErrorCode.IO_ERROR, e.getMessage());
    }
  }

  // -------------------- Upload 1 video Cloudinary --------------------
  @Operation(summary = "Upload single video (Cloudinary)", description = "Upload one video file and store it on Cloudinary.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Video uploaded successfully",
      content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(responseCode = "400", description = "Upload failed")
  })
  @PostMapping(value = "/cloudinary/video", consumes = "multipart/form-data")
  public AppApiResponse uploadVideoCloudinary(
    @Parameter(description = "Video file to upload", required = true)
    @RequestParam("video") MultipartFile video
  ) {
    try {
      String url = fileUploadService.uploadVideoCloudinary(video);
      return new AppApiResponse(url);
    } catch (Exception e) {
      throw new AppException(ErrorCode.IO_ERROR, e.getMessage());
    }
  }


  // -------------------- Upload nhiều video Cloudinary --------------------
  @Operation(summary = "Upload multiple videos (Cloudinary)", description = "Upload multiple video files and store them on Cloudinary.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Videos uploaded successfully",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
    @ApiResponse(responseCode = "400", description = "Upload failed")
  })
  @PostMapping(value = "/cloudinary/video/multiple", consumes = "multipart/form-data")
  public AppApiResponse uploadMultipleVideosCloudinary(
    @Parameter(description = "Video files to upload", required = true)
    @RequestParam("videos") List<MultipartFile> videos
  ) {
    try {
      List<String> urls = fileUploadService.uploadMultipleVideosCloudinary(videos);
      return new AppApiResponse(urls);
    } catch (Exception e) {
      throw new AppException(ErrorCode.IO_ERROR, e.getMessage());
    }
  }

}
