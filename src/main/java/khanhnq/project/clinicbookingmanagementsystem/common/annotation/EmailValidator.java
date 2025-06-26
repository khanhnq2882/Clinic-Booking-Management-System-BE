package khanhnq.project.clinicbookingmanagementsystem.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

public class EmailValidator implements ConstraintValidator<EmailConstraint, String> {
    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return Objects.nonNull(email) && email.matches("^\\w{3,}.{0,1}\\w{0,}@{1}\\w{2,}.{1}\\w{2,}.{0,1}\\w{2,}$");
    }
}
