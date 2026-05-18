package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.service.PayOsGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

@Service
@RequiredArgsConstructor
public class PayOsGatewayImpl implements PayOsGateway {

    private final PayOS payOS;

    @Override
    public CreatePaymentLinkResponse createPaymentLink(CreatePaymentLinkRequest request) {
        return payOS.paymentRequests().create(request);
    }

    @Override
    public WebhookData verifyWebhook(Webhook webhook) {
        return payOS.webhooks().verify(webhook);
    }
}
