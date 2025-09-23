package org.example.commonweb.util;

import java.util.concurrent.ThreadLocalRandom;

public final class Ids {
    private Ids() {}

    public static String randomNumeric(int length) {
        var sb = new StringBuilder(length);
        var random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static String otp() {
        return randomNumeric(6);
    }
}
