package com.lumen.awsspringbootservice.service;

import java.time.LocalDateTime;

public interface PurchaseReportService {

    /**
     * Generates a report for all purchases within the specified period and sends it via email
     * to the recipients defined in the application configuration.
     * <p>
     * If either {@code to} or {@code from} parameters are {@code null},
     * default values will be substituted based on the configured report period duration.
     *
     * @param to   the end of the reporting period; if {@code null}, the current time is used.
     * @param from the start of the reporting period; if {@code null}, it is computed as {@code to - reportPeriod}.
     */
    void generateAndSendReport(LocalDateTime to, LocalDateTime from);
}
