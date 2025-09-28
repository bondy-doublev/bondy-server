package org.example.mailservice.service.interfaces;

import org.example.commonweb.DTO.request.MailRequest;
import org.example.commonweb.enums.MailPurpose;

import java.util.Locale;
import java.util.Map;

public interface IMailService {
    void send(MailRequest req);
    default void send(MailPurpose purpose, String to, Map<String,Object> model, Locale locale) {
        send(MailRequest.builder()
                .to(to)
                .template(purpose.template)
                .model(model)
                .locale(locale == null ? Locale.ENGLISH : locale)
                .build());
    }
}
