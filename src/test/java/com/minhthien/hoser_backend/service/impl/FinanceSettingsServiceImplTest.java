package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.dto.request.FinanceSettingsRequest;
import com.minhthien.hoser_backend.entity.FinanceSettings;
import com.minhthien.hoser_backend.exception.BadRequestException;
import com.minhthien.hoser_backend.repository.FinanceSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceSettingsServiceImplTest {
    @Mock
    private FinanceSettingsRepository financeSettingsRepository;

    @Test
    void getFinanceSettingsCreatesDefaultWhenMissing() {
        FinanceSettingsServiceImpl service = new FinanceSettingsServiceImpl(financeSettingsRepository);
        when(financeSettingsRepository.findById(FinanceSettings.SINGLETON_ID)).thenReturn(Optional.empty());
        when(financeSettingsRepository.save(any(FinanceSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.getFinanceSettings();

        assertThat(response.getJockeyHireTaxPercent()).isEqualByComparingTo("10.00");
    }

    @Test
    void updateFinanceSettingsNormalizesPercent() {
        FinanceSettingsServiceImpl service = new FinanceSettingsServiceImpl(financeSettingsRepository);
        FinanceSettings settings = FinanceSettings.builder()
                .id(FinanceSettings.SINGLETON_ID)
                .jockeyHireTaxPercent(new BigDecimal("10.00"))
                .build();
        when(financeSettingsRepository.findById(FinanceSettings.SINGLETON_ID)).thenReturn(Optional.of(settings));
        when(financeSettingsRepository.save(any(FinanceSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.updateFinanceSettings(request("12.345"), "admin");

        assertThat(response.getJockeyHireTaxPercent()).isEqualByComparingTo("12.35");
        assertThat(settings.getUpdatedBy()).isEqualTo("admin");
    }

    @Test
    void updateFinanceSettingsKeepsExistingPercentWhenNotProvided() {
        FinanceSettingsServiceImpl service = new FinanceSettingsServiceImpl(financeSettingsRepository);
        FinanceSettings settings = FinanceSettings.builder()
                .id(FinanceSettings.SINGLETON_ID)
                .jockeyHireTaxPercent(new BigDecimal("10.00"))
                .build();
        FinanceSettingsRequest request = new FinanceSettingsRequest();

        when(financeSettingsRepository.findById(FinanceSettings.SINGLETON_ID)).thenReturn(Optional.of(settings));
        when(financeSettingsRepository.save(any(FinanceSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.updateFinanceSettings(request, "admin");

        assertThat(response.getJockeyHireTaxPercent()).isEqualByComparingTo("10.00");
        assertThat(settings.getUpdatedBy()).isEqualTo("admin");
    }

    @Test
    void updateFinanceSettingsRejectsOutOfRangePercent() {
        FinanceSettingsServiceImpl service = new FinanceSettingsServiceImpl(financeSettingsRepository);

        assertThatThrownBy(() -> service.updateFinanceSettings(request("100.01"), "admin"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Jockey hire tax percent must be between 0 and 100");

        verify(financeSettingsRepository, never()).save(any(FinanceSettings.class));
    }

    private FinanceSettingsRequest request(String percent) {
        FinanceSettingsRequest request = new FinanceSettingsRequest();
        request.setJockeyHireTaxPercent(new BigDecimal(percent));
        return request;
    }
}
