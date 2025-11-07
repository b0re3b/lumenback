package com.lumen.awsspringbootservice.dto.request.report;

import com.lumen.awsspringbootservice.validator.annotation.ValidDateRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@ValidDateRange
@Schema(description = "Request for manual report generation, specifying optional date range boundaries")
public class ReportRequest {

    @Schema(description = "Start of the reporting period (inclusive)",
            example = "2025-10-01T00:00:00", nullable = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @PastOrPresent(message = "'from' date cannot be in the future.")
    private LocalDateTime from;

    @Schema(description = "End of the reporting period (inclusive)",
            example = "2025-10-31T23:59:59", nullable = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @PastOrPresent(message = "'to' date cannot be in the future.")
    private LocalDateTime to;
}
