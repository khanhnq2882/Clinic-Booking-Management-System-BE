package khanhnq.project.clinicbookingmanagementsystem.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class DateValidator implements ConstraintValidator<DateConstraint, String> {

    private Boolean isOptional;

    @Override
    public void initialize(DateConstraint constraintAnnotation) {
        this.isOptional = constraintAnnotation.optional();
    }

    @Override
    public boolean isValid(String date, ConstraintValidatorContext constraintValidatorContext) {
        boolean validDate = isValidFormat("yyyy/MM/dd", date);
        return isOptional ? (validDate || (Objects.isNull(date))) : validDate;
    }

    private static boolean isValidFormat(String format, String value) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            if (value != null) {
                date = sdf.parse(value);
                if (!value.equals(sdf.format(date))) {
                    date = null;
                }
            }
        } catch (ParseException ex) {
        }
        return date != null;
    }
}
