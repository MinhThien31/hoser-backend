package com.minhthien.hoser_backend.service;

import com.minhthien.hoser_backend.dto.request.FinanceSettingsRequest;
import com.minhthien.hoser_backend.dto.response.FinanceSettingsResponse;

import java.math.BigDecimal;

public interface FinanceSettingsService {
    FinanceSettingsResponse getFinanceSettings();

    FinanceSettingsResponse updateFinanceSettings(FinanceSettingsRequest request, String updatedBy);

    BigDecimal getJockeyHireTaxPercent();
}
