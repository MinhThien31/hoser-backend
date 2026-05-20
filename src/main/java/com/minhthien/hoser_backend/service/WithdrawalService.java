package com.minhthien.hoser_backend.service;

import com.minhthien.hoser_backend.dto.request.AdminWalletWithdrawalRequest;
import com.minhthien.hoser_backend.dto.request.CreateWithdrawalRequest;
import com.minhthien.hoser_backend.dto.request.WithdrawalDecisionRequest;
import com.minhthien.hoser_backend.dto.response.AdminWalletWithdrawalResponse;
import com.minhthien.hoser_backend.dto.response.WithdrawalResponse;
import com.minhthien.hoser_backend.enums.WithdrawalStatus;

import java.util.List;

public interface WithdrawalService {
    WithdrawalResponse createUserWithdrawal(Long userId, CreateWithdrawalRequest request);

    List<WithdrawalResponse> getUserWithdrawals(Long userId);

    WithdrawalResponse getUserWithdrawal(Long userId, Long withdrawalId);

    List<WithdrawalResponse> getAdminWithdrawals(WithdrawalStatus status);

    WithdrawalResponse getAdminWithdrawal(Long withdrawalId);

    WithdrawalResponse approveWithdrawal(Long withdrawalId, Long adminId, WithdrawalDecisionRequest request);

    WithdrawalResponse rejectWithdrawal(Long withdrawalId, Long adminId, WithdrawalDecisionRequest request);

    WithdrawalResponse markWithdrawalPaid(Long withdrawalId, Long adminId, WithdrawalDecisionRequest request);

    AdminWalletWithdrawalResponse createAdminWithdrawal(Long adminId, AdminWalletWithdrawalRequest request);

    List<AdminWalletWithdrawalResponse> getAdminWalletWithdrawals();
}
