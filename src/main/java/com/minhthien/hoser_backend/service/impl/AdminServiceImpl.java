package com.minhthien.hoser_backend.service.impl;


import com.minhthien.hoser_backend.dto.response.AdminPayoutDebtResponse;
import com.minhthien.hoser_backend.dto.response.AdminPayoutDebtSummaryResponse;
import com.minhthien.hoser_backend.dto.response.UserResponse;
import com.minhthien.hoser_backend.entity.JockeyChallengeResult;
import com.minhthien.hoser_backend.entity.RaceResult;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.RacePayoutStatus;
import com.minhthien.hoser_backend.enums.RoleApprovalStatus;
import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.exception.ResourceNotFoundException;
import com.minhthien.hoser_backend.repository.JockeyChallengeResultRepository;
import com.minhthien.hoser_backend.repository.RaceResultRepository;
import com.minhthien.hoser_backend.repository.UserRepository;
import com.minhthien.hoser_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RaceResultRepository raceResultRepository;
    private final JockeyChallengeResultRepository jockeyChallengeResultRepository;

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<UserResponse> getActiveUsers() {
        return userRepository.findByActive(true).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<UserResponse> getDeactivatedUsers() {
        return userRepository.findByActive(false).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setActive(active);
        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setRole(role);
        if (role == UserRole.USER) {
            user.setPendingRole(null);
            user.setRoleApprovalStatus(RoleApprovalStatus.NONE);
            user.setRoleReviewReason(null);
            user.setRoleReviewedBy(null);
            user.setRoleReviewedAt(null);
        } else {
            user.setPendingRole(role);
            user.setRoleApprovalStatus(RoleApprovalStatus.APPROVED);
            user.setRoleReviewReason(null);
        }
        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPayoutDebtSummaryResponse getPayoutDebts() {
        List<AdminPayoutDebtResponse> debts = new ArrayList<>();

        raceResultRepository.findByPayoutStatusOrderByFinalizedAtAscIdAsc(RacePayoutStatus.UNPAID).stream()
                .map(this::mapRaceDebt)
                .forEach(debts::add);

        jockeyChallengeResultRepository.findByPayoutStatusOrderByFinalizedAtAscIdAsc(RacePayoutStatus.UNPAID).stream()
                .map(this::mapJockeyChallengeDebt)
                .forEach(debts::add);

        debts.sort(Comparator
                .comparing(AdminPayoutDebtResponse::getFinalizedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AdminPayoutDebtResponse::getDebtType, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AdminPayoutDebtResponse::getReferenceId, Comparator.nullsLast(Comparator.naturalOrder())));

        BigDecimal totalAmount = debts.stream()
                .map(AdminPayoutDebtResponse::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminPayoutDebtSummaryResponse.builder()
                .totalAmount(totalAmount)
                .debtCount(debts.size())
                .debts(debts)
                .build();
    }

    private AdminPayoutDebtResponse mapRaceDebt(RaceResult result) {
        return AdminPayoutDebtResponse.builder()
                .debtType("RACE_PRIZE")
                .referenceId(result.getId())
                .tournamentId(result.getRace().getTournament().getId())
                .tournamentName(result.getRace().getTournament().getName())
                .raceId(result.getRace().getId())
                .raceName(result.getRace().getName())
                .recipientUserId(result.getOwner().getId())
                .recipientUsername(result.getOwner().getUsername())
                .recipientRole("OWNER_AND_JOCKEY")
                .horseId(result.getHorse().getId())
                .horseName(result.getHorse().getName())
                .jockeyId(result.getJockey().getId())
                .jockeyUsername(result.getJockey().getUsername())
                .rank(result.getRank())
                .amount(result.getPrizeAmount())
                .ownerPrizeAmount(ownerRacePrizeAmount(result))
                .jockeyPrizeAmount(defaultZero(result.getJockeyPrizeAmount()))
                .jockeyPrizePercent(defaultZero(result.getJockeyPrizePercent()))
                .finalizedAt(result.getFinalizedAt())
                .note("Race prize is unpaid because admin wallet did not have enough balance")
                .build();
    }

    private AdminPayoutDebtResponse mapJockeyChallengeDebt(JockeyChallengeResult result) {
        return AdminPayoutDebtResponse.builder()
                .debtType("JOCKEY_CHALLENGE_PRIZE")
                .referenceId(result.getId())
                .tournamentId(result.getTournament().getId())
                .tournamentName(result.getTournament().getName())
                .recipientUserId(result.getJockey().getId())
                .recipientUsername(result.getJockey().getUsername())
                .recipientRole("JOCKEY")
                .rank(result.getChallengeRank())
                .amount(result.getPrizeAmount())
                .finalizedAt(result.getFinalizedAt())
                .note("Jockey challenge prize is unpaid because admin wallet did not have enough balance")
                .build();
    }

    private BigDecimal ownerRacePrizeAmount(RaceResult result) {
        BigDecimal ownerAmount = defaultZero(result.getOwnerPrizeAmount());
        BigDecimal jockeyAmount = defaultZero(result.getJockeyPrizeAmount());
        if (ownerAmount.compareTo(BigDecimal.ZERO) == 0 && jockeyAmount.compareTo(BigDecimal.ZERO) == 0) {
            return defaultZero(result.getPrizeAmount());
        }
        return ownerAmount;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .pendingRole(user.getPendingRole())
                .roleApprovalStatus(user.getRoleApprovalStatus())
                .roleReviewReason(user.getRoleReviewReason())
                .roleReviewedBy(user.getRoleReviewedBy())
                .roleReviewedAt(user.getRoleReviewedAt())
                .active(user.getActive())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
