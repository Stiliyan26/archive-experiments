package bg.latona.santa.selfie.util;

import bg.latona.santa.reports.ReportException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1Utils {
    public static String calculateSHA1(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            byte[] digest = md.digest(content);
            StringBuilder sb = new StringBuilder();

            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new ReportException("SHA-1 algorithm was not found");
        }
    }
}
