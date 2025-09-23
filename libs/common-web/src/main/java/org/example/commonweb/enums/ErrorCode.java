package org.example.commonweb.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    VALIDATION_ERROR(400),
    ENTITY_NOT_FOUND(404),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    INTERNAL_ERROR(500),
    BAD_REQUEST(400),
    DUPLICATE_RESOURCE(409),
    CONFLICT(409),
    TIMEOUT(408),
    SERVICE_UNAVAILABLE(503),
    IO_ERROR(500),

    MAIL_ERROR(500);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }
}
