package com.minhthien.hoser_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

@Configuration
public class PayOsConfig {

    @Bean
    public PayOS payOS(
            @Value("${payos.client-id}") String clientId,
            @Value("${payos.api-key}") String apiKey,
            @Value("${payos.checksum-key}") String checksumKey,
            @Value("${payos.base-url:https://api-merchant.payos.vn}") String baseUrl) {
        return new PayOS(ClientOptions.builder()
                .clientId(clientId)
                .apiKey(apiKey)
                .checksumKey(checksumKey)
                .baseURL(baseUrl)
                .build());
    }
}
