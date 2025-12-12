package org.example.interactionservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.commonweb.DTO.core.AppApiResponse;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.exception.AppException;
import org.example.interactionservice.config.security.ContextUser;
import org.example.interactionservice.dto.PageRequestDto;
import org.example.interactionservice.entity.MediaAttachment;
import org.example.interactionservice.repository.MediaAttachmentRepository;
import org.example.interactionservice.service.interfaces.IWallService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Media")
@RestController
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MediaController {
  IWallService wallService;
  MediaAttachmentRepository mediaAttachmentRepository;

  @GetMapping("/wall/{userId}/medias")
  AppApiResponse getWallMedia(@PathVariable Long userId, @ModelAttribute @Valid PageRequestDto filter) {
    List<MediaAttachment> medias = wallService.getWallMedia(userId, filter.toPageable());

    return new AppApiResponse(medias);
  }

  @DeleteMapping("/medias/{mediaId}")
  AppApiResponse deleteMedia(@PathVariable Long mediaId) {
    int deleted = mediaAttachmentRepository.deleteByIdAndUserId(mediaId, ContextUser.get().getUserId());

    if (deleted != 1)
      throw new AppException(ErrorCode.BAD_REQUEST, "Can not delete this media");

    return new AppApiResponse();
  }
}
