package com.minhthien.hoser_backend.dto.request;

import com.minhthien.hoser_backend.enums.PaymentOrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepositCallbackRequest {
    @NotBlank(message = "Reference code is required")
    private String referenceCode;

    @NotNull(message = "Status is required")
    private PaymentOrderStatus status;

    @NotBlank(message = "Callback token is required")
    private String callbackToken;

    private String providerTransactionId;

    private String metadata;
}
