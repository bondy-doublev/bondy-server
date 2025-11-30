package org.example.commonweb.enums.moderation;

import java.util.EnumSet;
import java.util.Set;

public enum ActionType {

  // ===== REPORT =====
  DISMISS_REPORT(EnumSet.of(TargetType.REPORT)),
  ACCEPT_REPORT(EnumSet.of(TargetType.REPORT)),
  ESCALATE_REPORT(EnumSet.of(TargetType.REPORT)),

  // ===== NỘI DUNG (POST / COMMENT / MESSAGE) =====
  HIDE_CONTENT(EnumSet.of(TargetType.POST, TargetType.COMMENT, TargetType.MESSAGE)),
  DELETE_CONTENT(EnumSet.of(TargetType.POST, TargetType.COMMENT, TargetType.MESSAGE)),
  EDIT_CONTENT(EnumSet.of(TargetType.POST, TargetType.COMMENT, TargetType.MESSAGE)),
  MASK_SENSITIVE_DATA(EnumSet.of(TargetType.POST, TargetType.COMMENT, TargetType.MESSAGE)),

  // ===== USER =====
  WARN_USER(EnumSet.of(TargetType.USER)),
  MUTE_USER(EnumSet.of(TargetType.USER)),
  SUSPEND_USER(EnumSet.of(TargetType.USER)),
  BAN_USER(EnumSet.of(TargetType.USER)),
  LIMIT_FEATURES(EnumSet.of(TargetType.USER)),

  // ===== KHÁC =====
  ADD_INTERNAL_NOTE(EnumSet.of(
    TargetType.REPORT,
    TargetType.USER,
    TargetType.POST,
    TargetType.COMMENT,
    TargetType.MESSAGE
  ));

  private final Set<TargetType> supportedTargets;

  ActionType(Set<TargetType> supportedTargets) {
    this.supportedTargets = supportedTargets;
  }

  public boolean supports(TargetType targetType) {
    return supportedTargets.contains(targetType);
  }
}
