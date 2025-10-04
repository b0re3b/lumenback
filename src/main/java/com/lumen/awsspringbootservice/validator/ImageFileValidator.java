package com.lumen.awsspringbootservice.validator;

import com.lumen.awsspringbootservice.validator.annotation.ValidImageFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public class ImageFileValidator implements ConstraintValidator<ValidImageFile, MultipartFile> {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/jpg"
    );

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        return ALLOWED_CONTENT_TYPES.contains(file.getContentType());
    }
}
