package com.minhthien.hoser_backend.repository;

import com.minhthien.hoser_backend.entity.WithdrawalRequest;
import com.minhthien.hoser_backend.enums.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {
    List<WithdrawalRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<WithdrawalRequest> findByStatusOrderByCreatedAtDesc(WithdrawalStatus status);

    List<WithdrawalRequest> findAllByOrderByCreatedAtDesc();
}
