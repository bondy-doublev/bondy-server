package org.example.authservice.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.service.interfaces.IUserService;
import org.example.commonweb.DTO.core.ApiResponse;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Tag(name = "User")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    IUserService userService;

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse uploadAvatar(@Parameter(hidden = true) @RequestHeader(value = "X-User-Id", required = false) Long userId,
                             @RequestParam("file") MultipartFile file)
    {
        String avatarUrl = userService.uploadAvatar(file, userId);

        return new ApiResponse(Map.of("avatarUrl", avatarUrl));
    }
}
