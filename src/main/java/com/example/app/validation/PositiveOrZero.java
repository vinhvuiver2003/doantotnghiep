package com.example.app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PositiveOrZeroValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PositiveOrZero {
    String message() default "Giá trị phải lớn hơn hoặc bằng 0";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 