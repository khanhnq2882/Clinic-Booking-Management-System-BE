package khanhnq.project.clinicbookingmanagementsystem.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Constraint(validatedBy = PasswordValidator.class)
public @interface PasswordConstraint {
    String message() default "Password must have at least 8 characters, must contain lowercase letters, uppercase letters, numbers and contain at least 1 special character.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
