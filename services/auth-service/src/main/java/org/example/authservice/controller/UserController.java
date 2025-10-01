package org.example.authservice.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.authservice.config.security.ContextUser;
import org.example.authservice.service.interfaces.IUserService;
import org.example.commonweb.DTO.core.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "User")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    IUserService userService;

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse uploadAvatar(@RequestParam("file") MultipartFile file)
    {
        String avatarUrl = userService.uploadAvatar(file, ContextUser.get().getUserId());

        return new ApiResponse(Map.of("avatarUrl", avatarUrl));
    }
}
