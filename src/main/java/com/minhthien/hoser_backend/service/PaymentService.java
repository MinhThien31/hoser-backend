package com.minhthien.hoser_backend.service;

import com.minhthien.hoser_backend.dto.request.CreateDepositOrderRequest;
import com.minhthien.hoser_backend.dto.request.DepositCallbackRequest;
import com.minhthien.hoser_backend.dto.response.PaymentCallbackLogResponse;
import com.minhthien.hoser_backend.dto.response.PaymentOrderResponse;
import vn.payos.model.webhooks.Webhook;

import java.util.List;

public interface PaymentService {
    PaymentOrderResponse createDepositOrder(Long userId, CreateDepositOrderRequest request);

    List<PaymentOrderResponse> getUserDepositOrders(Long userId);

    List<PaymentOrderResponse> getAdminPaymentOrders();

    List<PaymentCallbackLogResponse> getAdminPaymentCallbackLogs();

    PaymentOrderResponse handleDepositCallback(DepositCallbackRequest request);

    PaymentOrderResponse handlePayOsWebhook(Webhook webhook);
}
