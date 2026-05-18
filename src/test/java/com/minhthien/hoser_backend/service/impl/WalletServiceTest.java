package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.entity.Wallet;
import com.minhthien.hoser_backend.entity.WalletTransaction;
import com.minhthien.hoser_backend.enums.*;
import com.minhthien.hoser_backend.exception.BadRequestException;
import com.minhthien.hoser_backend.repository.UserRepository;
import com.minhthien.hoser_backend.repository.WalletRepository;
import com.minhthien.hoser_backend.repository.WalletTransactionRepository;
import com.minhthien.hoser_backend.service.WalletLedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletLedgerService walletLedgerService;

    private WalletServiceImpl walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletServiceImpl(
                walletRepository,
                walletTransactionRepository,
                userRepository,
                walletLedgerService
        );
    }

    @Test
    void creditIncreasesAvailableBalanceAndWritesLedger() {
        Wallet wallet = userWallet(new BigDecimal("100.00"), BigDecimal.ZERO);
        when(walletTransactionRepository.findByIdempotencyKey("credit-1")).thenReturn(Optional.empty());
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(walletLedgerService.record(eq(wallet), eq(WalletTransactionType.DEPOSIT), eq(WalletTransactionDirection.CREDIT),
                eq(new BigDecimal("50.00")), eq(new BigDecimal("100.00")), eq(new BigDecimal("150.00")),
                eq(BigDecimal.ZERO), eq(BigDecimal.ZERO), eq("TEST"), eq("1"), eq("credit-1"),
                isNull(), eq("credit"))).thenReturn(WalletTransaction.builder().id(10L).wallet(wallet).build());

        walletService.credit(1L, new BigDecimal("50.00"), WalletTransactionType.DEPOSIT,
                "TEST", "1", "credit-1", null, "credit");

        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("150.00");
        assertThat(wallet.getHoldBalance()).isEqualByComparingTo("0.00");
        verify(walletRepository).save(wallet);
        verify(walletLedgerService).record(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void debitDecreasesAvailableBalance() {
        Wallet wallet = userWallet(new BigDecimal("100.00"), BigDecimal.ZERO);
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(walletLedgerService.record(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any())).thenReturn(WalletTransaction.builder().wallet(wallet).build());

        walletService.debit(1L, new BigDecimal("40.00"), WalletTransactionType.WITHDRAW,
                "TEST", "1", null, null, null);

        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("60.00");
        assertThat(wallet.getHoldBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void holdMovesAvailableToHold() {
        Wallet wallet = userWallet(new BigDecimal("100.00"), BigDecimal.ZERO);
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(walletLedgerService.record(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any())).thenReturn(WalletTransaction.builder().wallet(wallet).build());

        walletService.hold(1L, new BigDecimal("25.00"), WalletTransactionType.ENTRY_FEE,
                "REGISTRATION", "1", null, null, null);

        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("75.00");
        assertThat(wallet.getHoldBalance()).isEqualByComparingTo("25.00");
    }

    @Test
    void releaseMovesHoldBackToAvailable() {
        Wallet wallet = userWallet(new BigDecimal("75.00"), new BigDecimal("25.00"));
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(walletLedgerService.record(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any())).thenReturn(WalletTransaction.builder().wallet(wallet).build());

        walletService.release(1L, new BigDecimal("10.00"), WalletTransactionType.REFUND,
                "REGISTRATION", "1", null, null, null);

        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("85.00");
        assertThat(wallet.getHoldBalance()).isEqualByComparingTo("15.00");
    }

    @Test
    void captureSubtractsHoldBalance() {
        Wallet wallet = userWallet(new BigDecimal("75.00"), new BigDecimal("25.00"));
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(walletLedgerService.record(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any())).thenReturn(WalletTransaction.builder().wallet(wallet).build());

        walletService.capture(1L, new BigDecimal("20.00"), WalletTransactionType.ENTRY_FEE,
                "REGISTRATION", "1", null, null, null);

        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("75.00");
        assertThat(wallet.getHoldBalance()).isEqualByComparingTo("5.00");
    }

    @Test
    void debitRejectsInsufficientAvailableBalance() {
        Wallet wallet = userWallet(new BigDecimal("10.00"), BigDecimal.ZERO);
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.debit(1L, new BigDecimal("11.00"),
                WalletTransactionType.WITHDRAW, "TEST", "1", null, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Wallet balance is insufficient");

        verify(walletLedgerService, never()).record(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void releaseRejectsInsufficientHoldBalance() {
        Wallet wallet = userWallet(new BigDecimal("10.00"), new BigDecimal("5.00"));
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.release(1L, new BigDecimal("6.00"),
                WalletTransactionType.REFUND, "TEST", "1", null, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Wallet hold balance is insufficient");

        verify(walletLedgerService, never()).record(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void lockedWalletRejectsMutation() {
        Wallet wallet = userWallet(new BigDecimal("100.00"), BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.LOCKED);
        when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.credit(1L, new BigDecimal("10.00"),
                WalletTransactionType.DEPOSIT, "TEST", "1", null, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Wallet is not active");

        verify(walletLedgerService, never()).record(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void duplicateIdempotencyKeyReturnsExistingTransactionWithoutMutation() {
        Wallet wallet = userWallet(new BigDecimal("100.00"), BigDecimal.ZERO);
        WalletTransaction existing = WalletTransaction.builder().id(99L).wallet(wallet).build();
        when(walletTransactionRepository.findByIdempotencyKey("same-key")).thenReturn(Optional.of(existing));

        WalletTransaction result = walletService.credit(1L, new BigDecimal("10.00"),
                WalletTransactionType.DEPOSIT, "TEST", "1", "same-key", null, null);

        assertThat(result).isSameAs(existing);
        verify(walletRepository, never()).findByUserIdForUpdate(anyLong());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(walletLedgerService, never()).record(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void adminWalletOperationsUpdateBalancesAndWriteLedger() {
        Wallet wallet = adminWallet(new BigDecimal("100.00"), BigDecimal.ZERO);
        when(walletRepository.findByOwnerTypeForUpdate(WalletOwnerType.ADMIN)).thenReturn(List.of(wallet));
        when(walletLedgerService.record(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any())).thenReturn(WalletTransaction.builder().wallet(wallet).build());

        walletService.creditAdmin(new BigDecimal("50.00"), WalletTransactionType.DEPOSIT,
                "TEST", "credit", null, null, null);
        walletService.debitAdmin(new BigDecimal("20.00"), WalletTransactionType.ADMIN_WITHDRAW,
                "TEST", "debit", null, null, null);
        walletService.holdAdmin(new BigDecimal("30.00"), WalletTransactionType.ADJUSTMENT,
                "TEST", "hold", null, null, null);
        walletService.releaseAdmin(new BigDecimal("10.00"), WalletTransactionType.REFUND,
                "TEST", "release", null, null, null);
        walletService.captureAdmin(new BigDecimal("20.00"), WalletTransactionType.ADJUSTMENT,
                "TEST", "capture", null, null, null);

        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("110.00");
        assertThat(wallet.getHoldBalance()).isEqualByComparingTo("0.00");
        verify(walletLedgerService, times(5)).record(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void adminWalletDuplicateIdempotencyKeyReturnsExistingTransactionWithoutMutation() {
        Wallet wallet = adminWallet(new BigDecimal("100.00"), BigDecimal.ZERO);
        WalletTransaction existing = WalletTransaction.builder().id(100L).wallet(wallet).build();
        when(walletTransactionRepository.findByIdempotencyKey("admin-same-key")).thenReturn(Optional.of(existing));

        WalletTransaction result = walletService.creditAdmin(new BigDecimal("10.00"), WalletTransactionType.DEPOSIT,
                "TEST", "1", "admin-same-key", null, null);

        assertThat(result).isSameAs(existing);
        verify(walletRepository, never()).findByOwnerTypeForUpdate(any());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(walletLedgerService, never()).record(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void createsLazyUserWalletWhenMissing() {
        User user = User.builder().id(1L).build();
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Wallet wallet = walletService.getOrCreateUserWallet(1L);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertThat(wallet).isSameAs(walletCaptor.getValue());
        assertThat(wallet.getOwnerType()).isEqualTo(WalletOwnerType.USER);
        assertThat(wallet.getUser()).isSameAs(user);
        assertThat(wallet.getCurrency()).isEqualTo("VND");
        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("0.00");
        assertThat(wallet.getHoldBalance()).isEqualByComparingTo("0.00");
    }

    private Wallet userWallet(BigDecimal availableBalance, BigDecimal holdBalance) {
        return Wallet.builder()
                .id(1L)
                .ownerType(WalletOwnerType.USER)
                .user(User.builder().id(1L).build())
                .currency("VND")
                .availableBalance(availableBalance)
                .holdBalance(holdBalance)
                .status(WalletStatus.ACTIVE)
                .build();
    }

    private Wallet adminWallet(BigDecimal availableBalance, BigDecimal holdBalance) {
        return Wallet.builder()
                .id(2L)
                .ownerType(WalletOwnerType.ADMIN)
                .currency("VND")
                .availableBalance(availableBalance)
                .holdBalance(holdBalance)
                .status(WalletStatus.ACTIVE)
                .build();
    }
}
