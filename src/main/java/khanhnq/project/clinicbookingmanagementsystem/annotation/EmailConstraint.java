package khanhnq.project.clinicbookingmanagementsystem.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Constraint(validatedBy = EmailValidator.class)
public @interface EmailConstraint {
    String message() default "Email must be a valid email.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
