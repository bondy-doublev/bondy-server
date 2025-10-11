package org.example.authservice.service.interfaces;

import org.example.authservice.dto.UpdateUserDto;
import org.example.authservice.dto.response.UserBasicResponse;
import org.example.authservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserService {
  User getProfile(Long userId);

  List<User> findByEmailContainingIgnoreCase(String email);

  String uploadAvatar(MultipartFile file, Long userId);

  User updateProfile(Long userId, UpdateUserDto user);

  List<UserBasicResponse> getBasicProfiles(List<Long> userIds);

  UserBasicResponse getBasicProfile(Long userId);

  List<User> getAllUsers();

  User editUser(Long userId, UpdateUserDto dto);

  User toggleStatus(Long userId);

  void deleteUser(Long userId);

  List<User> searchUsers(String address, String name);

  Page<UserBasicResponse> getAllBasicProfiles(int page);
}
