package com.minhthien.hoser_backend.config;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentProviderDataFixer implements CommandLineRunner {

    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        entityManager.createNativeQuery("""
                update payment_orders
                set provider = 'PAYOS'
                where provider in ('MANUAL', 'BANK_TRANSFER')
                """).executeUpdate();
    }
}
