package org.example.authservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.authservice.config.security.ContextUser;
import org.example.authservice.dto.UpdateUserDto;
import org.example.authservice.dto.request.BasicProfileRequest;
import org.example.authservice.dto.response.UserBasicResponse;
import org.example.authservice.entity.User;
import org.example.authservice.service.interfaces.IUserService;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "User")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
  IUserService userService;

  @GetMapping("/profile")
  AppApiResponse getProfile() {
    User user = userService.getProfile(ContextUser.get().getUserId());

    return new AppApiResponse(user);
  }

  @PutMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  AppApiResponse uploadAvatar(@RequestParam("file") MultipartFile file) {
    String avatarUrl = userService.uploadAvatar(file, ContextUser.get().getUserId());

    return new AppApiResponse(Map.of("avatarUrl", avatarUrl));
  }

  @PutMapping
  AppApiResponse updateProfile(@RequestBody @Valid UpdateUserDto dto) {
    User user = userService.updateProfile(ContextUser.get().getUserId(), dto);

    return new AppApiResponse(user);
  }

  @PostMapping("/basic-profiles")
  AppApiResponse getBasicProfile(@RequestBody @Valid BasicProfileRequest request) {
    List<UserBasicResponse> users = userService.getBasicProfile(request.getUserIds());

    return new AppApiResponse(users);
  }
}
