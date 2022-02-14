package com.jollykai.parkrunparser;

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
