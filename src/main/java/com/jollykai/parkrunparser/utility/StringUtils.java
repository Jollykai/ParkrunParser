package com.jollykai.parkrunparser.utility;

import java.util.Optional;

public class StringUtils {

    public static Optional<Integer> mayBeInteger(String number) {
        try {
            return Optional.of(Integer.parseInt(number));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
