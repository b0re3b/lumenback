package com.lumen.awsspringbootservice.validator;

import com.lumen.awsspringbootservice.dto.request.report.ReportRequest;
import com.lumen.awsspringbootservice.validator.annotation.ValidDateRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, ReportRequest> {

    @Override
    public boolean isValid(ReportRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value.getFrom() != null && value.getTo() != null) {
            return !value.getFrom().isAfter(value.getTo());
        }

        return true;
    }
}
