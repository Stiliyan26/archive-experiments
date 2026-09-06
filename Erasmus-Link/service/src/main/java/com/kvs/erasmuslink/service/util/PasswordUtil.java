package com.kvs.erasmuslink.service.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password related operations
 *
 * @author Venislav Kirilov
 */
public class PasswordUtil {

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
