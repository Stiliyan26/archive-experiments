package com.kvs.erasmuslink.data.model.enums;

/**
 * Describes the different types of review targets
 *
 * @author Venislav Kirilov
 */
public enum ReviewTargetType {
    ORGANIZATION,
    PROJECT;

    public static boolean contains(String value) {
        try {
            ReviewTargetType.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
