package com.minhthien.hoser_backend.service;

import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

public interface PayOsGateway {
    CreatePaymentLinkResponse createPaymentLink(CreatePaymentLinkRequest request);

    WebhookData verifyWebhook(Webhook webhook);
}
