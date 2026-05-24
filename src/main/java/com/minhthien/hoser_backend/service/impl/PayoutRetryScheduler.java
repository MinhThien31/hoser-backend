package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.entity.JockeyChallengeResult;
import com.minhthien.hoser_backend.entity.RaceResult;
import com.minhthien.hoser_backend.entity.Wallet;
import com.minhthien.hoser_backend.enums.RacePayoutStatus;
import com.minhthien.hoser_backend.enums.WalletTransactionType;
import com.minhthien.hoser_backend.repository.JockeyChallengeResultRepository;
import com.minhthien.hoser_backend.repository.RaceResultRepository;
import com.minhthien.hoser_backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutRetryScheduler {
    private static final String RACE_RESULT_REF = "RACE_RESULT";
    private static final String JOCKEY_CHALLENGE_REF = "JOCKEY_CHALLENGE";

    private final RaceResultRepository raceResultRepository;
    private final JockeyChallengeResultRepository jockeyChallengeResultRepository;
    private final WalletService walletService;

    @Scheduled(
            initialDelayString = "${app.payout.retry-initial-delay-ms:60000}",
            fixedDelayString = "${app.payout.retry-delay-ms:60000}"
    )
    @Transactional
    public void retryUnpaidPayouts() {
        int racePaid = retryRacePayouts();
        int challengePaid = retryChallengePayouts();
        if (racePaid > 0 || challengePaid > 0) {
            log.info("Retried unpaid payouts: racePaid={}, challengePaid={}", racePaid, challengePaid);
        }
    }

    private int retryRacePayouts() {
        int paid = 0;
        for (RaceResult result : raceResultRepository.findByPayoutStatusOrderByFinalizedAtAscIdAsc(RacePayoutStatus.UNPAID)) {
            BigDecimal amount = defaultZero(result.getPrizeAmount());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                result.setPayoutStatus(RacePayoutStatus.NOT_ELIGIBLE);
                raceResultRepository.save(result);
                continue;
            }
            if (!adminWalletCanPay(amount)) {
                break;
            }
            String referenceId = String.valueOf(result.getId());
            walletService.debitAdmin(amount, WalletTransactionType.PRIZE_PAYOUT,
                    RACE_RESULT_REF, referenceId,
                    "race-result:%d:admin-prize-debit".formatted(result.getId()),
                    null, "Race prize payout retry");
            BigDecimal ownerAmount = ownerRacePrizeAmount(result, amount);
            if (ownerAmount.compareTo(BigDecimal.ZERO) > 0) {
                walletService.credit(result.getOwner().getId(), ownerAmount, WalletTransactionType.PRIZE_PAYOUT,
                        RACE_RESULT_REF, referenceId,
                        "race-result:%d:owner-prize-credit".formatted(result.getId()),
                        null, "Race prize payout retry owner share");
            }
            BigDecimal jockeyAmount = defaultZero(result.getJockeyPrizeAmount());
            if (jockeyAmount.compareTo(BigDecimal.ZERO) > 0) {
                walletService.credit(result.getJockey().getId(), jockeyAmount, WalletTransactionType.PRIZE_PAYOUT,
                        RACE_RESULT_REF, referenceId,
                        "race-result:%d:jockey-prize-credit".formatted(result.getId()),
                        null, "Race prize payout retry jockey share");
            }
            result.setPayoutStatus(RacePayoutStatus.PAID);
            raceResultRepository.save(result);
            paid++;
        }
        return paid;
    }

    private int retryChallengePayouts() {
        int paid = 0;
        for (JockeyChallengeResult result : jockeyChallengeResultRepository.findByPayoutStatusOrderByFinalizedAtAscIdAsc(RacePayoutStatus.UNPAID)) {
            BigDecimal amount = defaultZero(result.getPrizeAmount());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                result.setPayoutStatus(RacePayoutStatus.NOT_ELIGIBLE);
                jockeyChallengeResultRepository.save(result);
                continue;
            }
            if (!adminWalletCanPay(amount)) {
                break;
            }
            String referenceId = "%d:%d".formatted(result.getTournament().getId(), result.getJockey().getId());
            walletService.debitAdmin(amount, WalletTransactionType.PRIZE_PAYOUT,
                    JOCKEY_CHALLENGE_REF, referenceId,
                    "jockey-challenge:%s:admin-prize-debit".formatted(referenceId),
                    null, "Jockey challenge prize payout retry");
            walletService.credit(result.getJockey().getId(), amount, WalletTransactionType.PRIZE_PAYOUT,
                    JOCKEY_CHALLENGE_REF, referenceId,
                    "jockey-challenge:%s:jockey-prize-credit".formatted(referenceId),
                    null, "Jockey challenge prize payout retry");
            result.setPayoutStatus(RacePayoutStatus.PAID);
            jockeyChallengeResultRepository.save(result);
            paid++;
        }
        return paid;
    }

    private boolean adminWalletCanPay(BigDecimal amount) {
        Wallet adminWallet = walletService.getOrCreateAdminWallet();
        return adminWallet.getAvailableBalance().compareTo(amount) >= 0;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal ownerRacePrizeAmount(RaceResult result, BigDecimal totalAmount) {
        BigDecimal ownerAmount = defaultZero(result.getOwnerPrizeAmount());
        BigDecimal jockeyAmount = defaultZero(result.getJockeyPrizeAmount());
        if (ownerAmount.compareTo(BigDecimal.ZERO) == 0 && jockeyAmount.compareTo(BigDecimal.ZERO) == 0) {
            return totalAmount;
        }
        return ownerAmount;
    }
}
