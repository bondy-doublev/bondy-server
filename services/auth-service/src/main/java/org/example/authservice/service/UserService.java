package org.example.authservice.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.authservice.client.UploadClient;
import org.example.authservice.dto.UpdateUserDto;
import org.example.authservice.entity.User;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.service.interfaces.IUserService;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserService implements IUserService {
  UserRepository userRepository;
  UploadClient uploadClient;

  @Override
  public User getProfile(Long userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Profile not found"));
  }

  @Override
  @Transactional
  public String uploadAvatar(MultipartFile file, Long userId) {
    if (userId == null) {
      throw new AppException(ErrorCode.BAD_REQUEST, "Your session is invalid, please log in again");
    }

    if (!userRepository.existsById(userId)) {
      throw new AppException(ErrorCode.ENTITY_NOT_FOUND, "User with ID " + userId + " not found.");
    }

    try {
      String avatarUrl = uploadClient.uploadAvatar(file);
      int updated = userRepository.updateAvatarUrlById(userId, avatarUrl);

      if (updated != 1) {
        throw new AppException(ErrorCode.INTERNAL_ERROR, "Upload avatar fail, please try again.");
      }

      return avatarUrl;
    } catch (Exception e) {
      throw new AppException(ErrorCode.IO_ERROR, e.getMessage());
    }
  }

  @Override
  public User updateProfile(Long userId, UpdateUserDto dto) {
    if (dto.getFirstName() == null
      && dto.getMiddleName() == null
      && dto.getLastName() == null
      && dto.getDob() == null
      && dto.getGender() == null) {
      throw new AppException(ErrorCode.VALIDATION_ERROR, "No data provided for update");
    }

    User user = userRepository.findById(userId)
      .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "User not found"));

    if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
    if (dto.getMiddleName() != null) user.setMiddleName(dto.getMiddleName());
    if (dto.getLastName() != null) user.setLastName(dto.getLastName());
    if (dto.getDob() != null) user.setDob(dto.getDob());
    if (dto.getGender() != null) user.setGender(dto.getGender());

    return userRepository.save(user);
  }

  @Override
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  public List<User> findByEmailContainingIgnoreCase(String email) {
    return userRepository.findByEmailContainingIgnoreCase(email);
  }


  @Override
  public User editUser(Long userId, UpdateUserDto dto) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "User not found"));

    if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
    if (dto.getMiddleName() != null) user.setMiddleName(dto.getMiddleName());
    if (dto.getLastName() != null) user.setLastName(dto.getLastName());
    if (dto.getDob() != null) user.setDob(dto.getDob());
    if (dto.getGender() != null) user.setGender(dto.getGender());
    if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());

    return userRepository.save(user);
  }

  @Override
  public User toggleStatus(Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "User not found"));

    user.setActive(!Boolean.TRUE.equals(user.getActive()));
    return userRepository.save(user);
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "User not found"));

    try {
      userRepository.delete(user);
    } catch (Exception e) {
      throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to delete user: " + e.getMessage());
    }
  }
}
