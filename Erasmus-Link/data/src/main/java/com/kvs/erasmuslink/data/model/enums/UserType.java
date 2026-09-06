package com.kvs.erasmuslink.data.model.enums;

/**
 * Describes the different types of users in the system
 *
 * @author Venislav Kirilov
 */
public enum UserType {
    ORGANIZATION,
    PARTICIPANT,
    ADMIN;

    public static boolean contains(String value) {
        try {
            UserType.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
