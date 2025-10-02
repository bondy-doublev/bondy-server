package org.example.interactionservice.validator.MaxSizeFromConfig;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxSizeFromConfigValidator.class)
public @interface MaxSizeFromConfig {
    String message() default "Size exceeds allowed limit";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String configKey();
}