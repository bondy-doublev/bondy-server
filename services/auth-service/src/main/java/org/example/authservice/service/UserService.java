package org.example.authservice.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.authservice.client.UploadClient;
import org.example.authservice.dto.UpdateUserDto;
import org.example.authservice.dto.response.UploadResponse;
import org.example.authservice.dto.response.UserBasicResponse;
import org.example.authservice.entity.User;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.service.interfaces.IUserService;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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

  public List<User> searchUsers(String address, String name) {
    Specification<User> spec = Specification.where(null);

    if (address != null && !address.isEmpty()) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("address"), address));
    }

    if (name != null && !name.isEmpty()) {
      spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("firstName")), "%" + name.toLowerCase() + "%"));
    }

    // có thể add nhiều filter khác vào spec tương tự

    return userRepository.findAll(spec);
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
      UploadResponse avatarUrl = uploadClient.uploadAvatar(file);
      System.out.println("Avatar: " + avatarUrl.getData());
      int updated = userRepository.updateAvatarUrlById(userId, avatarUrl.getData());

      if (updated != 1) {
        throw new AppException(ErrorCode.INTERNAL_ERROR, "Upload avatar fail, please try again.");
      }
      return avatarUrl.getData();
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
  public List<UserBasicResponse> getBasicProfiles(List<Long> userIds) {
    return userRepository.findBasicProfilesByIds(userIds);
  }

  public Page<UserBasicResponse> getAllBasicProfiles(int page) {
    Pageable pageable = PageRequest.of(page, 15); // 15 user / page
    return userRepository.findAllBasicProfiles(pageable);
  }

  @Override
  public void updateFriendCount(Long senderId, Long receiverId, int delta) {
    User sender = userRepository.findById(senderId)
      .orElseThrow(() -> new RuntimeException("User not found"));

    User receiver = userRepository.findById(receiverId)
      .orElseThrow(() -> new RuntimeException("User not found"));

    int newCountSender = Math.max(0, sender.getFriendCount() + delta);
    sender.setFriendCount(newCountSender);

    int newCountReceiver = Math.max(0, receiver.getFriendCount() + delta);
    receiver.setFriendCount(newCountReceiver);
    List<User> users = new ArrayList<>();
    users.add(sender);
    users.add(receiver);

    userRepository.saveAll(users);
  }

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

  @Override
  public UserBasicResponse getBasicProfile(Long userId) {
    return userRepository.findBasicProfileById(userId)
      .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "User with " + userId + " not found"));
  }
}
