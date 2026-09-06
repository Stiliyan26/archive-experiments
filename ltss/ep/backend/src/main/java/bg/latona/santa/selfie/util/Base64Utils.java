package bg.latona.santa.selfie.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public  class Base64Utils {

    public static String encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String input) {
        return new String(Base64.getDecoder().decode(input.getBytes(StandardCharsets.UTF_8)));
    }
}
