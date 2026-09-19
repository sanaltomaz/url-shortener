package com.tom.url_shortener.url.infrastructure.utils;

public class Base62 {
    private static final String ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();

    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException(
                    "Value must be greater than or equal to zero"
            );
        }
        if (value == 0) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            int remainder = (int) (value % BASE);
            sb.append(ALPHABET.charAt(remainder));
            value /= BASE;
        }

        return sb.reverse().toString();
    }

    public static long decode(String str) {
        if (str == null) {
            throw new IllegalArgumentException(
                    "String is null"
            );
        }

        long result = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int index = ALPHABET.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException(
                        "Invalid character: " + c
                );
            }
            result = (result * BASE) + index;
        }
        return result;
    }
}
