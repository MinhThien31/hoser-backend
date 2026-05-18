package com.minhthien.hoser_backend.repository;

import com.minhthien.hoser_backend.entity.AdminWalletWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminWalletWithdrawalRepository extends JpaRepository<AdminWalletWithdrawal, Long> {
    List<AdminWalletWithdrawal> findAllByOrderByCreatedAtDesc();
}
