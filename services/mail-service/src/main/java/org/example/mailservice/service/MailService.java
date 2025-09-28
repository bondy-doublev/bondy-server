package org.example.mailservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import lombok.RequiredArgsConstructor;
import org.example.commonweb.DTO.request.MailRequest;
import org.example.commonweb.enums.ErrorCode;
import org.example.commonweb.enums.MailPurpose;
import org.example.commonweb.exception.AppException;
import org.example.mailservice.property.PropsConfig;
import org.example.mailservice.service.interfaces.IMailService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MailService implements IMailService {
    private final JavaMailSender mailSender;
    @Qualifier("emailTemplateEngine")
    private final SpringTemplateEngine emailTemplateEngine;
    private final MessageSource messageSource;
    private final PropsConfig props;

    @Override
    public void send(MailRequest req) {
        try {
            var locale = req.getLocale() != null ? req.getLocale() : Locale.getDefault();
            Context ctx = new Context(locale);
            if (req.getModel() != null) req.getModel().forEach(ctx::setVariable);
            ctx.setVariable("baseUrl", props.getMail().getBaseUrl());
            String html = emailTemplateEngine.process(req.getTemplate(), ctx);

            MailPurpose purpose = MailPurpose.fromTemplate(req.getTemplate());
            String subject = props.getMail().getSubjects()
                        .getOrDefault(purpose.template, "[Bondy] Notification");


            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());

            setFromSmart(h, props.getMail().getFrom());
            if (props.getMail().getReplyTo() != null) {
                setReplyToSmart(h, props.getMail().getReplyTo());
            }

            h.setTo(req.getTo());
            h.setSubject(subject);
            h.setText(html, true);

            if (req.getAttachments() != null) {
                for (var a : req.getAttachments()) {
                    h.addAttachment(
                            MimeUtility.encodeText(a.getFilename(), StandardCharsets.UTF_8.name(), null),
                            new ByteArrayResource(a.getBytes()),
                            a.getContentType()
                    );
                }
            }

            mailSender.send(msg);
        } catch (Exception ex) {
            throw new AppException(ErrorCode.MAIL_ERROR, ex.getMessage());
        }
    }

    private void setFromSmart(MimeMessageHelper h, String from) throws MessagingException, UnsupportedEncodingException {
        int lt = from.indexOf('<');
        int gt = from.indexOf('>');
        if (lt >= 0 && gt > lt) {
            String personal = from.substring(0, lt).trim();
            String address  = from.substring(lt + 1, gt).trim();
            h.setFrom(new InternetAddress(address, personal, StandardCharsets.UTF_8.name()));
        } else {
            h.setFrom(from);
        }
    }

    private void setReplyToSmart(MimeMessageHelper h, String replyTo) throws MessagingException, UnsupportedEncodingException {
        int lt = replyTo.indexOf('<');
        int gt = replyTo.indexOf('>');
        if (lt >= 0 && gt > lt) {
            String personal = replyTo.substring(0, lt).trim();
            String address  = replyTo.substring(lt + 1, gt).trim();
            h.setReplyTo(new InternetAddress(address, personal, StandardCharsets.UTF_8.name()));
        } else {
            h.setReplyTo(replyTo);
        }
    }
}
