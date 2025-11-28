package com.lumen.awsspringbootservice.service;

import com.lumen.awsspringbootservice.exception.EmailSendingException;

import java.util.Map;

public interface SimpleEmailService {

    /**
     * Sends one or more generated reports to predefined recipients.
     * <p>
     * Each entry in the provided map represents a single attachment,
     * where the key is the filename (e.g., {@code "report.pdf"}) and
     * the value is the raw file content as a byte array.
     *
     * @param reports a map containing report filenames and their corresponding binary data;
     *                must not be {@code null} or empty.
     * @throws EmailSendingException if sending the email fails due to connection or SES-related issues.
     */
    void sendReports(Map<String, byte[]> reports);
}
