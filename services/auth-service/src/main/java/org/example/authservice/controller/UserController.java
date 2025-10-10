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
import org.springframework.data.domain.Page;
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

  @GetMapping("/{userId}/basic-profile")
  AppApiResponse getBasicProfile(@PathVariable Long userId) {
    UserBasicResponse user = userService.getBasicProfile(userId);

    return new AppApiResponse(user);
  }

  @PostMapping("/basic-profiles")
  AppApiResponse getBasicProfiles(@RequestBody @Valid BasicProfileRequest request) {
    List<UserBasicResponse> users = userService.getBasicProfiles(request.getUserIds());

    return new AppApiResponse(users);
  }

  @GetMapping("/all-basic-profiles")
  public AppApiResponse getAllBasicProfiles(@RequestParam(defaultValue = "0") int page) {
    Page<UserBasicResponse> usersPage = userService.getAllBasicProfiles(page);
    List<UserBasicResponse> users = usersPage.getContent(); // chỉ lấy list
    return new AppApiResponse(users); // trả về list như JSON mẫu
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

  @GetMapping("/search")
  public AppApiResponse searchUsers(
    @RequestParam(required = false) String address,
    @RequestParam(required = false) String name
  ) {
    List<User> users = userService.searchUsers(address, name);
    return new AppApiResponse(users);
  }
}
