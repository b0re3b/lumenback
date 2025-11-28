package com.lumen.awsspringbootservice.controller;

import com.lumen.awsspringbootservice.dto.request.report.ReportRequest;
import com.lumen.awsspringbootservice.dto.response.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Reports", description = "APIs for triggering and managing purchase reports")
@RequestMapping("api/v1/lumen/reports")
public interface ReportController {

    @Operation(
            summary = "Trigger manual report generation",
            description = """
                    Manually triggers the generation of a purchase report for a specified period.
                    If no dates are provided, the default reporting period is used (from system configuration).""",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Report generated and sent successfully",
                            content = @Content(schema = @Schema(implementation = MessageResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid date range (e.g. from > to or future dates)",
                            content = @Content(schema = @Schema(implementation = MessageResponse.class))
                    )
            }
    )
    @PostMapping("/trigger")
    ResponseEntity<MessageResponse> triggerReportGeneration(
            @Parameter(description = "Optional date range for report generation",
                    schema = @Schema(implementation = ReportRequest.class))
            @ModelAttribute @Valid ReportRequest request
    );
}
