package khanhnq.project.clinicbookingmanagementsystem.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumberConstraint, String> {
    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext constraintValidatorContext) {
        return phoneNumber.matches("^0{1}[2|3|5|7|8|9]{1}[0-9]{8,9}$");
    }
}
