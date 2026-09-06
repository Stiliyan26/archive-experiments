package bg.latona.santa.selfie.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtils {

    public static String formatDateToYYYYMMDD(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        return sdf.format(date);
    }
}
