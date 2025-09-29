package org.example.authservice.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.authservice.client.UploadClient;
import org.example.authservice.config.security.JwtService;
import org.example.authservice.repository.UserRepository;
import org.example.authservice.service.interfaces.IUserService;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserService implements IUserService {
    UserRepository userRepository;
    JwtService jwtService;
    UploadClient uploadClient;

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
}
