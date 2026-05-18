package com.minhthien.hoser_backend.service;

import com.minhthien.hoser_backend.dto.response.AdminAuditLogResponse;

import java.util.List;

public interface AdminAuditLogService {
    List<AdminAuditLogResponse> getAdminAuditLogs(String referenceType, String referenceId);
}
