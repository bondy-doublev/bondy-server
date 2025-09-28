package org.example.commonweb.DTO.request;

import lombok.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailRequest {
    private String to;
    private String template;
    private Map<String, Object> model;
    private Locale locale;
    private List<Attachment> attachments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Attachment {
        private String filename;
        private byte[] bytes;       // JSON sẽ là base64 -> Jackson tự decode vào byte[]
        private String contentType;
    }
}
