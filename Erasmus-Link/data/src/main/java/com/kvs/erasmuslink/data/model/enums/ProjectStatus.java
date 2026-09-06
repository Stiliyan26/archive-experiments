package com.kvs.erasmuslink.data.model.enums;

/**
 * Describes the different statuses a project can have
 *
 * @author Venislav Kirilov
 */
public enum ProjectStatus {
    OPEN,
    CLOSED;

    public static boolean contains(String value) {
        try {
            ProjectStatus.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
