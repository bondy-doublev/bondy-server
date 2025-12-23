package org.example.commonweb.enums;

import java.util.Arrays;

public enum MailPurpose {
  OTP_REGISTRATION("otp_registration"),
  OAUTH2_WELCOME("oauth2_welcome"),
  WELCOME("welcome"),
  RESET_PASSWORD_OTP("reset_password_otp");

  public final String template;

  MailPurpose(String template) {
    this.template = template;
  }

  public static MailPurpose fromTemplate(String t) {
    return Arrays.stream(values())
      .filter(p -> p.template.equalsIgnoreCase(t))
      .findFirst()
      .orElseThrow();
  }
}