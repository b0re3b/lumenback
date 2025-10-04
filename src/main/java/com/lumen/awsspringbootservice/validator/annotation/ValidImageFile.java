package com.lumen.awsspringbootservice.validator.annotation;

import com.lumen.awsspringbootservice.validator.ImageFileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ImageFileValidator.class)
@Documented
public @interface ValidImageFile {
    String message() default "Invalid image file. Allowed types: JPEG, PNG, JPG";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

