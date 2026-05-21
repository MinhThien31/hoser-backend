package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.dto.request.TournamentPrizeRequest;
import com.minhthien.hoser_backend.dto.request.TournamentRequest;
import com.minhthien.hoser_backend.dto.request.TournamentRoundRequest;
import com.minhthien.hoser_backend.dto.response.TournamentPrizeResponse;
import com.minhthien.hoser_backend.dto.response.TournamentResponse;
import com.minhthien.hoser_backend.dto.response.TournamentRoundResponse;
import com.minhthien.hoser_backend.entity.AdminAuditLog;
import com.minhthien.hoser_backend.entity.Tournament;
import com.minhthien.hoser_backend.entity.TournamentPrize;
import com.minhthien.hoser_backend.entity.TournamentRound;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.PrizeRecipientPolicy;
import com.minhthien.hoser_backend.enums.TournamentStatus;
import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.exception.BadRequestException;
import com.minhthien.hoser_backend.exception.ResourceNotFoundException;
import com.minhthien.hoser_backend.exception.UnauthorizedException;
import com.minhthien.hoser_backend.repository.AdminAuditLogRepository;
import com.minhthien.hoser_backend.repository.TournamentRepository;
import com.minhthien.hoser_backend.repository.UserRepository;
import com.minhthien.hoser_backend.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {
    private static final String REFERENCE_TYPE = "TOURNAMENT";

    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

    @Override
    @Transactional
    public TournamentResponse createTournament(Long adminId, TournamentRequest request) {
        User admin = requireAdmin(adminId);
        validateBaseRequest(request);

        Tournament tournament = Tournament.builder()
                .status(TournamentStatus.DRAFT)
                .createdBy(admin.getUsername())
                .updatedBy(admin.getUsername())
                .build();
        applyRequest(tournament, request, admin.getUsername());
        Tournament saved = tournamentRepository.save(tournament);
        recordAudit(admin, "TOURNAMENT_CREATED", saved, "Tournament draft created");
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TournamentResponse updateTournament(Long adminId, Long tournamentId, TournamentRequest request) {
        User admin = requireAdmin(adminId);
        validateBaseRequest(request);
        Tournament tournament = requireTournament(tournamentId);
        if (tournament.getStatus() != TournamentStatus.DRAFT && tournament.getStatus() != TournamentStatus.PUBLISHED) {
            throw new BadRequestException("Only draft or published tournaments can be updated");
        }

        applyRequest(tournament, request, admin.getUsername());
        Tournament saved = tournamentRepository.save(tournament);
        recordAudit(admin, "TOURNAMENT_UPDATED", saved, "Tournament setup updated");
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TournamentResponse updateTournamentStatus(Long adminId, Long tournamentId, TournamentStatus status) {
        User admin = requireAdmin(adminId);
        if (status == null) {
            throw new BadRequestException("Tournament status is required");
        }
        Tournament tournament = requireTournament(tournamentId);
        TournamentStatus oldStatus = tournament.getStatus();

        if (requiresReadySetup(status)) {
            validateReadyForPublish(tournament);
        }
        tournament.setStatus(status);
        if (status == TournamentStatus.PUBLISHED && tournament.getPublishedAt() == null) {
            tournament.setPublishedAt(LocalDateTime.now());
        }
        if (status == TournamentStatus.OPEN_REGISTRATION && tournament.getOpenedRegistrationAt() == null) {
            tournament.setOpenedRegistrationAt(LocalDateTime.now());
        }
        tournament.setUpdatedBy(admin.getUsername());
        Tournament saved = tournamentRepository.save(tournament);
        recordAudit(admin, "TOURNAMENT_STATUS_UPDATED", saved,
                "Tournament status changed from " + oldStatus + " to " + status);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TournamentResponse> getAdminTournaments(TournamentStatus status) {
        List<Tournament> tournaments = status == null
                ? tournamentRepository.findAllByOrderByCreatedAtDesc()
                : tournamentRepository.findByStatusOrderByCreatedAtDesc(status);
        return tournaments.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TournamentResponse getAdminTournament(Long tournamentId) {
        return mapToResponse(requireTournament(tournamentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TournamentResponse> getPublicTournaments() {
        return tournamentRepository.findByStatusInOrderByStartAtAsc(publicStatuses()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TournamentResponse getPublicTournament(Long tournamentId) {
        Tournament tournament = requireTournament(tournamentId);
        if (!isPublicStatus(tournament.getStatus())) {
            throw new ResourceNotFoundException("Tournament", "id", tournamentId);
        }
        return mapToResponse(tournament);
    }

    private void applyRequest(Tournament tournament, TournamentRequest request, String updatedBy) {
        tournament.setName(request.getName());
        tournament.setDescription(request.getDescription());
        tournament.setLocation(request.getLocation());
        tournament.setRegistrationOpenAt(request.getRegistrationOpenAt());
        tournament.setRegistrationCloseAt(request.getRegistrationCloseAt());
        tournament.setStartAt(request.getStartAt());
        tournament.setEndAt(request.getEndAt());
        tournament.setCheckInDeadlineAt(request.getCheckInDeadlineAt());
        tournament.setEntryFee(defaultZero(request.getEntryFee()));
        tournament.setDepositAmount(defaultZero(request.getDepositAmount()));
        tournament.setMinTeams(request.getMinTeams());
        tournament.setMaxTeams(request.getMaxTeams());
        tournament.setUpdatedBy(updatedBy);
        tournament.replaceRounds(mapRounds(request.getRounds()));
        tournament.replacePrizes(mapPrizes(request.getPrizes()));
    }

    private List<TournamentRound> mapRounds(List<TournamentRoundRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(request -> TournamentRound.builder()
                        .name(request.getName())
                        .roundOrder(request.getRoundOrder())
                        .raceCount(request.getRaceCount())
                        .minParticipantsPerRace(request.getMinParticipantsPerRace())
                        .maxParticipantsPerRace(request.getMaxParticipantsPerRace())
                        .advancementRuleType(request.getAdvancementRuleType())
                        .advancementCount(request.getAdvancementCount())
                        .note(request.getNote())
                        .build())
                .toList();
    }

    private List<TournamentPrize> mapPrizes(List<TournamentPrizeRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(request -> TournamentPrize.builder()
                        .rank(request.getRank())
                        .amount(defaultZero(request.getAmount()))
                        .itemName(request.getItemName())
                        .recipientPolicy(request.getRecipientPolicy() == null
                                ? PrizeRecipientPolicy.OWNER
                                : request.getRecipientPolicy())
                        .note(request.getNote())
                        .build())
                .toList();
    }

    private void validateBaseRequest(TournamentRequest request) {
        if (request == null) {
            throw new BadRequestException("Tournament request is required");
        }
        if (!hasText(request.getName())) {
            throw new BadRequestException("Tournament name is required");
        }
        if (!hasText(request.getLocation())) {
            throw new BadRequestException("Location is required");
        }
        validateTimeWindow(request.getRegistrationOpenAt(), request.getRegistrationCloseAt(),
                request.getStartAt(), request.getEndAt(), request.getCheckInDeadlineAt());
        validateTeamLimits(request.getMinTeams(), request.getMaxTeams());
        requireNonNegative(defaultZero(request.getEntryFee()), "Entry fee must not be negative");
        requireNonNegative(defaultZero(request.getDepositAmount()), "Deposit amount must not be negative");
    }

    private void validateReadyForPublish(Tournament tournament) {
        validateTimeWindow(tournament.getRegistrationOpenAt(), tournament.getRegistrationCloseAt(),
                tournament.getStartAt(), tournament.getEndAt(), tournament.getCheckInDeadlineAt());
        validateTeamLimits(tournament.getMinTeams(), tournament.getMaxTeams());
        requireNonNegative(defaultZero(tournament.getEntryFee()), "Entry fee must not be negative");
        requireNonNegative(defaultZero(tournament.getDepositAmount()), "Deposit amount must not be negative");

        if (tournament.getRounds() == null || tournament.getRounds().isEmpty()) {
            throw new BadRequestException("Tournament must have at least one round before publishing");
        }
        if (tournament.getPrizes() == null || tournament.getPrizes().isEmpty()) {
            throw new BadRequestException("Tournament must have at least one prize before publishing");
        }

        validateRounds(tournament);
        validatePrizes(tournament);
    }

    private void validateTimeWindow(LocalDateTime registrationOpenAt, LocalDateTime registrationCloseAt,
                                    LocalDateTime startAt, LocalDateTime endAt,
                                    LocalDateTime checkInDeadlineAt) {
        if (registrationOpenAt == null || registrationCloseAt == null || startAt == null || endAt == null) {
            throw new BadRequestException("Tournament time window is required");
        }
        if (!registrationOpenAt.isBefore(registrationCloseAt)) {
            throw new BadRequestException("Registration open time must be before close time");
        }
        if (registrationCloseAt.isAfter(startAt)) {
            throw new BadRequestException("Registration close time must not be after tournament start time");
        }
        if (!startAt.isBefore(endAt)) {
            throw new BadRequestException("Tournament start time must be before end time");
        }
        if (checkInDeadlineAt != null && checkInDeadlineAt.isAfter(startAt)) {
            throw new BadRequestException("Check-in deadline must not be after tournament start time");
        }
    }

    private void validateTeamLimits(Integer minTeams, Integer maxTeams) {
        if (minTeams == null || maxTeams == null) {
            throw new BadRequestException("Tournament team limits are required");
        }
        if (minTeams <= 0) {
            throw new BadRequestException("Minimum teams must be greater than zero");
        }
        if (maxTeams <= 0) {
            throw new BadRequestException("Maximum teams must be greater than zero");
        }
        if (minTeams > maxTeams) {
            throw new BadRequestException("Minimum teams must not exceed maximum teams");
        }
    }

    private void validateRounds(Tournament tournament) {
        Set<Integer> orders = new HashSet<>();
        List<TournamentRound> rounds = tournament.getRounds().stream()
                .sorted(Comparator.comparing(TournamentRound::getRoundOrder))
                .toList();
        for (TournamentRound round : rounds) {
            if (!hasText(round.getName())) {
                throw new BadRequestException("Round name is required");
            }
            if (round.getRoundOrder() == null || round.getRoundOrder() <= 0) {
                throw new BadRequestException("Round order must be greater than zero");
            }
            if (!orders.add(round.getRoundOrder())) {
                throw new BadRequestException("Round order must be unique within a tournament");
            }
            if (round.getRaceCount() == null || round.getRaceCount() <= 0) {
                throw new BadRequestException("Race count must be greater than zero");
            }
            if (round.getMinParticipantsPerRace() == null || round.getMinParticipantsPerRace() <= 0
                    || round.getMaxParticipantsPerRace() == null || round.getMaxParticipantsPerRace() <= 0) {
                throw new BadRequestException("Participants per race must be greater than zero");
            }
            if (round.getMinParticipantsPerRace() > round.getMaxParticipantsPerRace()) {
                throw new BadRequestException("Minimum participants per race must not exceed maximum participants per race");
            }
            if (round.getAdvancementCount() == null || round.getAdvancementCount() <= 0) {
                throw new BadRequestException("Advancement count must be greater than zero");
            }
            if (round.getAdvancementCount() > round.getMaxParticipantsPerRace()) {
                throw new BadRequestException("Advancement count must not exceed maximum participants per race");
            }
        }

        TournamentRound firstRound = rounds.get(0);
        int firstRoundCapacity = firstRound.getRaceCount() * firstRound.getMaxParticipantsPerRace();
        if (firstRoundCapacity < tournament.getMinTeams()) {
            throw new BadRequestException("First round capacity must be at least tournament minimum teams");
        }

        for (int index = 0; index < rounds.size() - 1; index++) {
            TournamentRound current = rounds.get(index);
            TournamentRound next = rounds.get(index + 1);
            int advancementCapacity = current.getRaceCount() * current.getAdvancementCount();
            int nextMinimumNeed = next.getRaceCount() * next.getMinParticipantsPerRace();
            if (advancementCapacity < nextMinimumNeed) {
                throw new BadRequestException("Advancement rule cannot fill the next round minimum participants");
            }
        }
    }

    private void validatePrizes(Tournament tournament) {
        Set<Integer> ranks = new HashSet<>();
        for (TournamentPrize prize : tournament.getPrizes()) {
            if (prize.getRank() == null || prize.getRank() <= 0) {
                throw new BadRequestException("Prize rank must be greater than zero");
            }
            if (!ranks.add(prize.getRank())) {
                throw new BadRequestException("Prize rank must be unique within a tournament");
            }
            BigDecimal amount = defaultZero(prize.getAmount());
            requireNonNegative(amount, "Prize amount must not be negative");
            if (amount.compareTo(BigDecimal.ZERO) == 0 && !hasText(prize.getItemName())) {
                throw new BadRequestException("Prize must have a positive amount or item name");
            }
        }
    }

    private void recordAudit(User admin, String action, Tournament tournament, String reason) {
        adminAuditLogRepository.save(AdminAuditLog.builder()
                .adminId(admin.getId())
                .action(action)
                .referenceType(REFERENCE_TYPE)
                .referenceId(String.valueOf(tournament.getId()))
                .reason(reason)
                .metadata("status=" + tournament.getStatus())
                .build());
    }

    private Tournament requireTournament(Long tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", "id", tournamentId));
    }

    private User requireAdmin(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));
        if (admin.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only admins can manage tournaments");
        }
        return admin;
    }

    private TournamentResponse mapToResponse(Tournament tournament) {
        return TournamentResponse.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .description(tournament.getDescription())
                .location(tournament.getLocation())
                .registrationOpenAt(tournament.getRegistrationOpenAt())
                .registrationCloseAt(tournament.getRegistrationCloseAt())
                .startAt(tournament.getStartAt())
                .endAt(tournament.getEndAt())
                .checkInDeadlineAt(tournament.getCheckInDeadlineAt())
                .entryFee(tournament.getEntryFee())
                .depositAmount(tournament.getDepositAmount())
                .minTeams(tournament.getMinTeams())
                .maxTeams(tournament.getMaxTeams())
                .status(tournament.getStatus())
                .publishedAt(tournament.getPublishedAt())
                .openedRegistrationAt(tournament.getOpenedRegistrationAt())
                .rounds(tournament.getRounds().stream()
                        .sorted(Comparator.comparing(TournamentRound::getRoundOrder))
                        .map(this::mapRound)
                        .toList())
                .prizes(tournament.getPrizes().stream()
                        .sorted(Comparator.comparing(TournamentPrize::getRank))
                        .map(this::mapPrize)
                        .toList())
                .createdAt(tournament.getCreatedAt())
                .updatedAt(tournament.getUpdatedAt())
                .createdBy(tournament.getCreatedBy())
                .updatedBy(tournament.getUpdatedBy())
                .build();
    }

    private TournamentRoundResponse mapRound(TournamentRound round) {
        return TournamentRoundResponse.builder()
                .id(round.getId())
                .name(round.getName())
                .roundOrder(round.getRoundOrder())
                .raceCount(round.getRaceCount())
                .minParticipantsPerRace(round.getMinParticipantsPerRace())
                .maxParticipantsPerRace(round.getMaxParticipantsPerRace())
                .advancementRuleType(round.getAdvancementRuleType())
                .advancementCount(round.getAdvancementCount())
                .status(round.getStatus())
                .note(round.getNote())
                .createdAt(round.getCreatedAt())
                .updatedAt(round.getUpdatedAt())
                .build();
    }

    private TournamentPrizeResponse mapPrize(TournamentPrize prize) {
        return TournamentPrizeResponse.builder()
                .id(prize.getId())
                .rank(prize.getRank())
                .amount(prize.getAmount())
                .itemName(prize.getItemName())
                .recipientPolicy(prize.getRecipientPolicy())
                .note(prize.getNote())
                .createdAt(prize.getCreatedAt())
                .updatedAt(prize.getUpdatedAt())
                .build();
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void requireNonNegative(BigDecimal value, String message) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(message);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isPublicStatus(TournamentStatus status) {
        return publicStatuses().contains(status);
    }

    private boolean requiresReadySetup(TournamentStatus status) {
        return status == TournamentStatus.PUBLISHED || status == TournamentStatus.OPEN_REGISTRATION;
    }

    private List<TournamentStatus> publicStatuses() {
        return List.of(
                TournamentStatus.PUBLISHED,
                TournamentStatus.OPEN_REGISTRATION,
                TournamentStatus.REGISTRATION_CLOSED,
                TournamentStatus.SCHEDULED,
                TournamentStatus.ONGOING,
                TournamentStatus.COMPLETED
        );
    }
}
