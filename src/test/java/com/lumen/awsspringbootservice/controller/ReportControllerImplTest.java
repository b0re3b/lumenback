package com.lumen.awsspringbootservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumen.awsspringbootservice.controller.impl.ReportControllerImpl;
import com.lumen.awsspringbootservice.service.impl.PurchaseReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ReportControllerImpl Tests")
class ReportControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PurchaseReportService purchaseReportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /reports/trigger should call service and return success message")
    void shouldTriggerReportGenerationSuccessfully() throws Exception {
        // given
        LocalDateTime from = LocalDateTime.of(2025, 11, 1, 10, 0);
        LocalDateTime to = LocalDateTime.of(2025, 11, 7, 10, 0);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        // when & then
        mockMvc.perform(multipart("/api/v1/lumen/reports/trigger")
                        .param("from", from.format(formatter))
                        .param("to", to.format(formatter))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Report generated and sent successfully."));

        // verify
        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(purchaseReportService, times(1))
                .generateAndSendReport(fromCaptor.capture(), toCaptor.capture());

        assertThat(fromCaptor.getValue()).isEqualTo(from);
        assertThat(toCaptor.getValue()).isEqualTo(to);
    }
}
