package com.minhthien.hoser_backend.service;

import com.minhthien.hoser_backend.dto.request.AdminWalletWithdrawalRequest;
import com.minhthien.hoser_backend.dto.request.CreateWithdrawalRequest;
import com.minhthien.hoser_backend.dto.request.WithdrawalDecisionRequest;
import com.minhthien.hoser_backend.dto.response.AdminWalletWithdrawalResponse;
import com.minhthien.hoser_backend.dto.response.WithdrawalResponse;

import java.util.List;

public interface WithdrawalService {
    WithdrawalResponse createUserWithdrawal(Long userId, CreateWithdrawalRequest request);

    List<WithdrawalResponse> getUserWithdrawals(Long userId);

    List<WithdrawalResponse> getAdminWithdrawals();

    WithdrawalResponse approveWithdrawal(Long withdrawalId, Long adminId, WithdrawalDecisionRequest request);

    WithdrawalResponse rejectWithdrawal(Long withdrawalId, Long adminId, WithdrawalDecisionRequest request);

    WithdrawalResponse markWithdrawalPaid(Long withdrawalId, Long adminId, WithdrawalDecisionRequest request);

    AdminWalletWithdrawalResponse createAdminWithdrawal(Long adminId, AdminWalletWithdrawalRequest request);

    List<AdminWalletWithdrawalResponse> getAdminWalletWithdrawals();
}
