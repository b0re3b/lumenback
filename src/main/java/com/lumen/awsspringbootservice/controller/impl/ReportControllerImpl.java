package com.lumen.awsspringbootservice.controller.impl;

import com.lumen.awsspringbootservice.controller.ReportController;
import com.lumen.awsspringbootservice.dto.request.report.ReportRequest;
import com.lumen.awsspringbootservice.dto.response.MessageResponse;
import com.lumen.awsspringbootservice.service.impl.PurchaseReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/lumen/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportControllerImpl implements ReportController {

    private final PurchaseReportService purchaseReportService;

    @PostMapping("/trigger")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageResponse> triggerReportGeneration(@Valid @ModelAttribute ReportRequest request) {
        log.info("Manual report trigger requested by admin. from={}, to={}", request.getFrom(), request.getTo());
        purchaseReportService.generateAndSendReport(request.getFrom(), request.getTo());

        return ResponseEntity.status(HttpStatus.OK).body(
                MessageResponse.builder()
                        .message("Report generated and sent successfully.")
                        .build()
        );
    }
}
