package com.minhthien.hoser_backend.repository;

import com.minhthien.hoser_backend.entity.PaymentCallbackLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentCallbackLogRepository extends JpaRepository<PaymentCallbackLog, Long> {
    List<PaymentCallbackLog> findAllByOrderByProcessedAtDesc();

    List<PaymentCallbackLog> findByReferenceCodeOrderByProcessedAtDesc(String referenceCode);
}
