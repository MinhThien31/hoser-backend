package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.dto.response.AdminAuditLogResponse;
import com.minhthien.hoser_backend.entity.AdminAuditLog;
import com.minhthien.hoser_backend.repository.AdminAuditLogRepository;
import com.minhthien.hoser_backend.service.AdminAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAuditLogServiceImpl implements AdminAuditLogService {

    private final AdminAuditLogRepository adminAuditLogRepository;

    @Override
    public List<AdminAuditLogResponse> getAdminAuditLogs(String referenceType, String referenceId) {
        List<AdminAuditLog> logs;
        if (hasText(referenceType) && hasText(referenceId)) {
            logs = adminAuditLogRepository.findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(referenceType, referenceId);
        } else {
            logs = adminAuditLogRepository.findAllByOrderByCreatedAtDesc();
        }
        return logs.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private AdminAuditLogResponse mapToResponse(AdminAuditLog log) {
        return AdminAuditLogResponse.builder()
                .id(log.getId())
                .adminId(log.getAdminId())
                .action(log.getAction())
                .referenceType(log.getReferenceType())
                .referenceId(log.getReferenceId())
                .amount(log.getAmount())
                .reason(log.getReason())
                .metadata(log.getMetadata())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
