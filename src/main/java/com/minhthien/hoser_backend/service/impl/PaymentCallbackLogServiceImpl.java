package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.dto.request.DepositCallbackRequest;
import com.minhthien.hoser_backend.dto.response.PaymentCallbackLogResponse;
import com.minhthien.hoser_backend.entity.PaymentCallbackLog;
import com.minhthien.hoser_backend.repository.PaymentCallbackLogRepository;
import com.minhthien.hoser_backend.service.PaymentCallbackLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCallbackLogServiceImpl implements PaymentCallbackLogService {

    private final PaymentCallbackLogRepository paymentCallbackLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(DepositCallbackRequest request, boolean tokenValid, boolean processed, String errorMessage) {
        paymentCallbackLogRepository.save(PaymentCallbackLog.builder()
                .referenceCode(request.getReferenceCode())
                .status(request.getStatus())
                .providerTransactionId(request.getProviderTransactionId())
                .tokenValid(tokenValid)
                .processed(processed)
                .errorMessage(truncate(errorMessage))
                .metadata(request.getMetadata())
                .build());
    }

    @Override
    public List<PaymentCallbackLogResponse> getAdminPaymentCallbackLogs() {
        return paymentCallbackLogRepository.findAllByOrderByProcessedAtDesc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PaymentCallbackLogResponse mapToResponse(PaymentCallbackLog log) {
        return PaymentCallbackLogResponse.builder()
                .id(log.getId())
                .referenceCode(log.getReferenceCode())
                .status(log.getStatus())
                .providerTransactionId(log.getProviderTransactionId())
                .tokenValid(log.isTokenValid())
                .processed(log.isProcessed())
                .errorMessage(log.getErrorMessage())
                .metadata(log.getMetadata())
                .processedAt(log.getProcessedAt())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private String truncate(String value) {
        if (value == null || value.length() <= 500) {
            return value;
        }
        return value.substring(0, 500);
    }
}
