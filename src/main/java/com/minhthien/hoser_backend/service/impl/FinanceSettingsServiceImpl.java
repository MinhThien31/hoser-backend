package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.dto.request.FinanceSettingsRequest;
import com.minhthien.hoser_backend.dto.response.FinanceSettingsResponse;
import com.minhthien.hoser_backend.entity.FinanceSettings;
import com.minhthien.hoser_backend.exception.BadRequestException;
import com.minhthien.hoser_backend.repository.FinanceSettingsRepository;
import com.minhthien.hoser_backend.service.FinanceSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class FinanceSettingsServiceImpl implements FinanceSettingsService {
    private static final BigDecimal MIN_PERCENT = new BigDecimal("0.00");
    private static final BigDecimal MAX_PERCENT = new BigDecimal("100.00");

    private final FinanceSettingsRepository financeSettingsRepository;

    @Override
    @Transactional
    public FinanceSettingsResponse getFinanceSettings() {
        return mapToResponse(getOrCreateSettings());
    }

    @Override
    @Transactional
    public FinanceSettingsResponse updateFinanceSettings(FinanceSettingsRequest request, String updatedBy) {
        if (request == null) {
            throw new BadRequestException("Finance settings request is required");
        }
        BigDecimal percent = request.getJockeyHireTaxPercent() == null
                ? null
                : normalizePercent(request.getJockeyHireTaxPercent());
        FinanceSettings settings = getOrCreateSettings();
        if (percent != null) {
            settings.setJockeyHireTaxPercent(percent);
        }
        settings.setUpdatedBy(updatedBy);
        return mapToResponse(financeSettingsRepository.save(settings));
    }

    @Override
    @Transactional
    public BigDecimal getJockeyHireTaxPercent() {
        return getOrCreateSettings().getJockeyHireTaxPercent();
    }

    private FinanceSettings getOrCreateSettings() {
        return financeSettingsRepository.findById(FinanceSettings.SINGLETON_ID)
                .orElseGet(() -> financeSettingsRepository.save(FinanceSettings.builder()
                        .id(FinanceSettings.SINGLETON_ID)
                        .jockeyHireTaxPercent(FinanceSettings.DEFAULT_JOCKEY_HIRE_TAX_PERCENT)
                        .build()));
    }

    private BigDecimal normalizePercent(BigDecimal percent) {
        if (percent.compareTo(MIN_PERCENT) < 0 || percent.compareTo(MAX_PERCENT) > 0) {
            throw new BadRequestException("Jockey hire tax percent must be between 0 and 100");
        }
        return percent.setScale(2, RoundingMode.HALF_UP);
    }

    private FinanceSettingsResponse mapToResponse(FinanceSettings settings) {
        return FinanceSettingsResponse.builder()
                .jockeyHireTaxPercent(settings.getJockeyHireTaxPercent())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}
