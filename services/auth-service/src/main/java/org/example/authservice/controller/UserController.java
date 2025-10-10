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

  @GetMapping
  public AppApiResponse getAllUsers(@RequestParam(name = "email", required = false) String email) {
    List<User> users;
    if (email != null && !email.isBlank()) {
      users = userService.findByEmailContainingIgnoreCase(email);
    } else {
      users = userService.getAllUsers();
    }
    return new AppApiResponse(users);
  }


  @PutMapping("/{id}")
  AppApiResponse editUser(@PathVariable("id") Long id, @RequestBody @Valid UpdateUserDto dto) {
    User user = userService.editUser(id, dto);
    return new AppApiResponse(user);
  }

  @PutMapping("/{id}/toggle-status")
  AppApiResponse toggleUserStatus(@PathVariable("id") Long id) {
    User user = userService.toggleStatus(id);
    return new AppApiResponse(Map.of(
      "userId", user.getId(),
      "active", user.getActive()
    ));
  }

  @DeleteMapping("/{id}")
  AppApiResponse deleteUser(@PathVariable("id") Long id) {
    userService.deleteUser(id);
    return new AppApiResponse(Map.of(
      "message", "User deleted successfully",
      "userId", id
    ));
  }
}
