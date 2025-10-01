package org.example.authservice.service.interfaces;

import org.example.authservice.dto.UpdateUserDto;
import org.example.authservice.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    User getProfile(Long userId);
    String uploadAvatar(MultipartFile file, Long userId);
    User updateProfile(Long userId, UpdateUserDto user);
}
