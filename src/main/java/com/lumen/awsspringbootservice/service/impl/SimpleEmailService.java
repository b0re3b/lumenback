package com.lumen.awsspringbootservice.service.impl;

import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleEmailService {

    private final SesClient sesClient;

    @Value("${app.report.recipients}")
    private String[] recipients;

    @Value("${app.report.sender:no-reply@lumen.com}")
    private String sender;

    public void sendReports(Map<String, byte[]> reports) {
        try {
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            for (String recipient : recipients) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            }
            message.setSubject("🎬 Weekly Movie Purchase Report");

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Attached are automatically generated reports for this week.");

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);

            for (Map.Entry<String, byte[]> entry : reports.entrySet()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setFileName(entry.getKey());
                attachmentPart.setDataHandler(new DataHandler(
                        new ByteArrayDataSource(entry.getValue(), "application/octet-stream")));
                multipart.addBodyPart(attachmentPart);
            }

            message.setContent(multipart);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);
            RawMessage rawMessage = RawMessage.builder()
                    .data(SdkBytes.fromByteArray(outputStream.toByteArray()))
                    .build();

            SendRawEmailRequest request = SendRawEmailRequest.builder()
                    .rawMessage(rawMessage)
                    .build();

            sesClient.sendRawEmail(request);
            log.info("Report email successfully sent via AWS SES to: {}", (Object) recipients);
        } catch (Exception e) {
            log.error("Failed to send report via AWS SES", e);
        }
    }
}
