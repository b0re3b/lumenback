package com.lumen.awsspringbootservice.scheduler;

import com.lumen.awsspringbootservice.service.PurchaseReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseReportScheduler {

    private final PurchaseReportService purchaseReportService;

    @Value("${app.report.enabled:true}")
    private boolean reportEnabled;

    @Value("${app.report.schedule.cron:0 59 23 * * SUN}")
    private String cronExpression;

    @Scheduled(cron = "${app.report.schedule.cron}", zone = "Europe/Kyiv")
    public void generateWeeklyReport() {
        if (!reportEnabled) {
            log.info("Automatic report generation is disabled (app.report.enabled=false)");
            return;
        }

        log.info("Starting automatic report generation (CRON: {})", cronExpression);
        try {
            purchaseReportService.generateAndSendReport(null, null);
            log.info("Report successfully generated and sent.");
        } catch (Exception e) {
            log.error("Failed to generate or send the report", e);
        }
    }
}
