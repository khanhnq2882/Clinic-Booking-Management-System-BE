package khanhnq.project.clinicbookingmanagementsystem.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<PasswordConstraint, String> {
    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        return password.matches("^[\\w\\p{P}\\p{S}{1,}]{8,}$");
    }
}
