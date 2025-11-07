package com.lumen.awsspringbootservice.controller.impl;

import com.lumen.awsspringbootservice.dto.request.purchase.CreatePaymentSessionRequest;
import com.lumen.awsspringbootservice.dto.response.purchase.CreatePaymentSessionResponse;
import com.lumen.awsspringbootservice.service.MockPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/mock/payments")
@RequiredArgsConstructor
public class MockPaymentController {

    private final MockPaymentService mockPaymentService;

    @Value("${server.host:localhost}")
    private String serverHost;

    @Value("${server.port:8080}")
    private String serverPort;


    @PostMapping("/create-session")
    public ResponseEntity<CreatePaymentSessionResponse> createSession(@RequestBody CreatePaymentSessionRequest request) {

        String sessionId = mockPaymentService.createPaymentSession(request);
        String paymentUrl = "http://" + serverHost + ":" + serverPort + "/api/v1/mock/payments/checkout?sessionId=" + sessionId;
        return ResponseEntity.ok(new CreatePaymentSessionResponse(paymentUrl));
    }

    @GetMapping(value = "/checkout", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> checkout(@RequestParam String sessionId) {
        mockPaymentService.getPaymentSessionById(sessionId);
        String html = """
                <html>
                  <body style="font-family: sans-serif; text-align:center;">
                    <h2>Mock Payment Gateway</h2>
                    <p>Session: %s</p>
                    <a href="http://localhost:8080/api/v1/mock/payments/confirm?sessionId=%s">Confirm Payment</a><br><br>
                    <a href="http://localhost:8080/api/v1/mock/payments/cancel?sessionId=%s">Cancel Payment</a>
                  </body>
                </html>
                """.formatted(sessionId, sessionId, sessionId);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    @PostMapping(value = "/confirm", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> confirm(@RequestParam String sessionId) {
        mockPaymentService.approvePaymentSession(sessionId);

        return ResponseEntity.ok("""
                    <html><body style="text-align:center;">
                    <h3>Payment successful!</h3>
                    <p>You can close this window now.</p>
                    </body></html>
                """);
    }

    @PostMapping(value = "/cancel", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> cancel(@RequestParam String sessionId) {
        mockPaymentService.cancelPaymentSession(sessionId);

        return ResponseEntity.ok("""
                    <html><body style="text-align:center;">
                    <h3>Payment cancelled!</h3>
                    <p>You can close this window now.</p>
                    </body></html>
                """);
    }
}
