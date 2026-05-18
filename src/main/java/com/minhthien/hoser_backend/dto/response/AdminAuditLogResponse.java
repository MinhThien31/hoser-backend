package com.minhthien.hoser_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminAuditLogResponse {
    private Long id;
    private Long adminId;
    private String action;
    private String referenceType;
    private String referenceId;
    private BigDecimal amount;
    private String reason;
    private String metadata;
    private LocalDateTime createdAt;
}
