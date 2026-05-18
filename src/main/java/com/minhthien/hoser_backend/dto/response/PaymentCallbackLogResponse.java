package com.minhthien.hoser_backend.dto.response;

import com.minhthien.hoser_backend.enums.PaymentOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentCallbackLogResponse {
    private Long id;
    private String referenceCode;
    private PaymentOrderStatus status;
    private String providerTransactionId;
    private boolean tokenValid;
    private boolean processed;
    private String errorMessage;
    private String metadata;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
