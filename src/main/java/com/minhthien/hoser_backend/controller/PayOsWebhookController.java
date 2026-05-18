package com.minhthien.hoser_backend.controller;

import com.minhthien.hoser_backend.dto.response.ApiResponse;
import com.minhthien.hoser_backend.dto.response.PaymentOrderResponse;
import com.minhthien.hoser_backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.model.webhooks.Webhook;

@RestController
@RequestMapping("/api/payos")
@RequiredArgsConstructor
public class PayOsWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> handlePayOsWebhook(@RequestBody Webhook webhook) {
        return ResponseEntity.ok(ApiResponse.success("payOS webhook processed",
                paymentService.handlePayOsWebhook(webhook)));
    }
}
