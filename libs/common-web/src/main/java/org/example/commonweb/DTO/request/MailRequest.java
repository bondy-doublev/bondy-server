package org.example.commonweb.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Builder
@Data
public class MailRequest {
    private String to;
    private String subject;
    private String template;
    private Map<String, Object> model;
    private Locale locale;
    private List<Attachment> attachments; // optional

    @Data @AllArgsConstructor
    public static class Attachment { String filename; byte[] bytes; String contentType; }
}
