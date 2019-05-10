package com.deep3.medusLabs.utilities;

import org.springframework.messaging.handler.annotation.Payload;

import javax.validation.Constraint;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordComplexityValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordComplexityConstraint {
    String message() default "Password does not meet complexity requirements.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
