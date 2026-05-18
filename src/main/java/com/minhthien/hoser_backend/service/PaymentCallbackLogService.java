package com.minhthien.hoser_backend.service;

import com.minhthien.hoser_backend.dto.request.DepositCallbackRequest;
import com.minhthien.hoser_backend.dto.response.PaymentCallbackLogResponse;

import java.util.List;

public interface PaymentCallbackLogService {
    void record(DepositCallbackRequest request, boolean tokenValid, boolean processed, String errorMessage);

    List<PaymentCallbackLogResponse> getAdminPaymentCallbackLogs();
}
