package org.example.commonweb.enums;

import lombok.Getter;

@Getter
public enum SuccessCode {
    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NO_CONTENT(204);

    private final int code;

    SuccessCode(int code) {
        this.code = code;
    }
}
