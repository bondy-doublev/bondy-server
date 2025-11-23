package org.example.uploadservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileUploadService {

  private final Path root;
  private final Cloudinary cloudinary;

  public FileUploadService(
    @Value("${cloudinary.cloud_name}") String cloudName,
    @Value("${cloudinary.api_key}") String apiKey,
    @Value("${cloudinary.api_secret}") String apiSecret,
    @Value("${upload.path}") String uploadPath
  ) {
    // Folder uploads: resolve tuyệt đối và normalize
    this.root = Paths.get(uploadPath).toAbsolutePath().normalize();

    // Tạo folder nếu chưa tồn tại
    try {
      if (!Files.exists(root)) {
        Files.createDirectories(root);
      }
    } catch (IOException e) {
      throw new RuntimeException("Cannot create upload directory: " + root, e);
    }

    this.cloudinary = new Cloudinary(ObjectUtils.asMap(
      "cloud_name", cloudName,
      "api_key", apiKey,
      "api_secret", apiSecret
    ));
  }

  // Sinh tên file unique: timestamp + random 5 ký tự + tên gốc
  private String generateUniqueFileName(String original) {
    String random = UUID.randomUUID().toString().substring(0, 5);
    return System.currentTimeMillis() + "_" + random + "_" + original;
  }

  // Upload 1 file local
  public String uploadFileLocal(MultipartFile file) throws IOException {
    String newFileName = generateUniqueFileName(file.getOriginalFilename());
    Path filePath = root.resolve(newFileName).normalize();
    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    // Trả về path tuyệt đối sạch
    return filePath.toAbsolutePath().toString();
  }

  // Upload nhiều file local
  public List<String> uploadMultipleFilesLocal(List<MultipartFile> files) throws IOException {
    List<String> results = new ArrayList<>();
    for (MultipartFile file : files) {
      results.add(uploadFileLocal(file));
    }
    return results;
  }

  // Upload 1 file Cloudinary
  public String uploadFileCloudinary(MultipartFile file) throws IOException {
    String newFileName = generateUniqueFileName(file.getOriginalFilename());

    Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
      ObjectUtils.asMap(
        "public_id", newFileName.replaceAll("\\.[^.]+$", ""),
        "resource_type", "auto"
      ));
    return uploadResult.get("secure_url").toString();
  }

  // Upload nhiều file Cloudinary
  public List<String> uploadMultipleFilesCloudinary(List<MultipartFile> files) throws IOException {
    List<String> urls = new ArrayList<>();
    for (MultipartFile file : files) {
      urls.add(uploadFileCloudinary(file));
    }
    return urls;
  }

  // Upload 1 video Cloudinary
  public String uploadVideoCloudinary(MultipartFile file) throws IOException {
    String newFileName = generateUniqueFileName(file.getOriginalFilename());

    Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
      ObjectUtils.asMap(
        "public_id", newFileName.replaceAll("\\.[^.]+$", ""),
        "resource_type", "video"
      ));

    return uploadResult.get("secure_url").toString();
  }

  // Upload nhiều video Cloudinary
  public List<String> uploadMultipleVideosCloudinary(List<MultipartFile> videos) throws IOException {
    List<String> urls = new ArrayList<>();
    for (MultipartFile video : videos) {
      urls.add(uploadVideoCloudinary(video));
    }
    return urls;
  }

}
