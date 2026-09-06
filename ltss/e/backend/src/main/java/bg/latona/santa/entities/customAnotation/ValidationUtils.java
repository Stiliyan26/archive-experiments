package bg.latona.santa.entities.customAnotation;

import bg.latona.santa.reports.ReportException;

import java.lang.reflect.Field;

public class ValidationUtils {

    public static boolean areAnnotatedFieldsNotNull(Object entity) {
        if (entity == null) {
            return false;
        }


        for (Field field : entity.getClass().getDeclaredFields()) {

            if (field.isAnnotationPresent(ValidateNotNull.class)) {
                field.setAccessible(true);

                try {
                    if (field.get(entity) == null) {
                        return false;
                    };

                } catch (IllegalAccessException e) {
                    throw new ReportException("Unable to access field " + field.getName());
                }
            }
        }

        return true;
    }
}
