package org.example.authservice.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    public String uploadAvatar(MultipartFile file, Long userId);
}
