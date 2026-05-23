package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.dto.request.TournamentPrizeRequest;
import com.minhthien.hoser_backend.dto.request.TournamentRequest;
import com.minhthien.hoser_backend.dto.request.TournamentRoundRequest;
import com.minhthien.hoser_backend.dto.request.TournamentUpdateRequest;
import com.minhthien.hoser_backend.entity.AdminAuditLog;
import com.minhthien.hoser_backend.entity.Tournament;
import com.minhthien.hoser_backend.entity.TournamentPrize;
import com.minhthien.hoser_backend.entity.TournamentRound;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.AdvancementRuleType;
import com.minhthien.hoser_backend.enums.PrizeRecipientPolicy;
import com.minhthien.hoser_backend.enums.TournamentStatus;
import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.exception.BadRequestException;
import com.minhthien.hoser_backend.exception.ResourceNotFoundException;
import com.minhthien.hoser_backend.exception.UnauthorizedException;
import com.minhthien.hoser_backend.repository.AdminAuditLogRepository;
import com.minhthien.hoser_backend.repository.TournamentRepository;
import com.minhthien.hoser_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Phase6TournamentServiceTest {
    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @Test
    void adminCreatesDraftTournamentWithRoundsAndPrizes() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> {
            Tournament tournament = invocation.getArgument(0);
            tournament.setId(10L);
            return tournament;
        });

        var response = service.createTournament(9L, request());

        assertThat(response.getStatus()).isEqualTo(TournamentStatus.DRAFT);
        assertThat(response.getRounds()).hasSize(2);
        assertThat(response.getPrizes()).hasSize(2);
        assertThat(response.getCreatedBy()).isEqualTo("admin");

        ArgumentCaptor<AdminAuditLog> auditCaptor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(adminAuditLogRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getAction()).isEqualTo("TOURNAMENT_CREATED");
        assertThat(auditCaptor.getValue().getReferenceId()).isEqualTo("10");
    }

    @Test
    void updatePublicStatusRejectsTournamentWithoutRoundConfig() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        Tournament tournament = tournament(TournamentStatus.DRAFT);
        tournament.replaceRounds(List.of());

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() -> service.updateTournamentStatus(9L, 10L, TournamentStatus.PUBLISHED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Tournament must have at least one round before publishing");
    }

    @Test
    void updateStatusMovesTournamentToPublishedAndOpenRegistration() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        Tournament tournament = tournament(TournamentStatus.DRAFT);

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var published = service.updateTournamentStatus(9L, 10L, TournamentStatus.PUBLISHED);
        assertThat(published.getStatus()).isEqualTo(TournamentStatus.PUBLISHED);
        assertThat(tournament.getPublishedAt()).isNotNull();

        var opened = service.updateTournamentStatus(9L, 10L, TournamentStatus.OPEN_REGISTRATION);
        assertThat(opened.getStatus()).isEqualTo(TournamentStatus.OPEN_REGISTRATION);
        assertThat(tournament.getOpenedRegistrationAt()).isNotNull();

        ArgumentCaptor<AdminAuditLog> auditCaptor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(adminAuditLogRepository, org.mockito.Mockito.times(2)).save(auditCaptor.capture());
        assertThat(auditCaptor.getAllValues())
                .extracting(AdminAuditLog::getAction)
                .containsOnly("TOURNAMENT_STATUS_UPDATED");
    }

    @Test
    void openRegistrationShortcutUsesReadyValidationAndStatusAudit() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        Tournament tournament = tournament(TournamentStatus.PUBLISHED);

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.openRegistration(9L, 10L);

        assertThat(response.getStatus()).isEqualTo(TournamentStatus.OPEN_REGISTRATION);
        assertThat(tournament.getOpenedRegistrationAt()).isNotNull();

        ArgumentCaptor<AdminAuditLog> auditCaptor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(adminAuditLogRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getAction()).isEqualTo("TOURNAMENT_STATUS_UPDATED");
    }

    @Test
    void openRegistrationShortcutRejectsTournamentWithoutPrizeConfig() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        Tournament tournament = tournament(TournamentStatus.PUBLISHED);
        tournament.replacePrizes(List.of());

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() -> service.openRegistration(9L, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Tournament must have at least one prize before publishing");
    }

    @Test
    void closeRegistrationShortcutMovesTournamentToRegistrationClosed() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        Tournament tournament = tournament(TournamentStatus.OPEN_REGISTRATION);

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.closeRegistration(9L, 10L);

        assertThat(response.getStatus()).isEqualTo(TournamentStatus.REGISTRATION_CLOSED);

        ArgumentCaptor<AdminAuditLog> auditCaptor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(adminAuditLogRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getAction()).isEqualTo("TOURNAMENT_STATUS_UPDATED");
    }

    @Test
    void updateTournamentKeepsRoundsAndPrizesWhenListsAreNotProvided() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        Tournament tournament = tournament(TournamentStatus.DRAFT);
        TournamentUpdateRequest request = new TournamentUpdateRequest();
        request.setDescription("Updated description");

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.updateTournament(9L, 10L, request);

        assertThat(response.getDescription()).isEqualTo("Updated description");
        assertThat(response.getName()).isEqualTo("Summer Derby");
        assertThat(response.getRounds()).hasSize(2);
        assertThat(response.getPrizes()).hasSize(1);
    }

    @Test
    void updateTournamentClearsRoundsAndPrizesWhenEmptyListsAreProvided() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        Tournament tournament = tournament(TournamentStatus.DRAFT);
        TournamentUpdateRequest request = new TournamentUpdateRequest();
        request.setRounds(List.of());
        request.setPrizes(List.of());

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.updateTournament(9L, 10L, request);

        assertThat(response.getRounds()).isEmpty();
        assertThat(response.getPrizes()).isEmpty();
    }

    @Test
    void updateTournamentReplacesRoundsAndPrizesWhenListsAreProvided() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        Tournament tournament = tournament(TournamentStatus.DRAFT);
        TournamentUpdateRequest request = new TournamentUpdateRequest();
        request.setRounds(List.of(round("Final Only", 1, 1, 2, 8, 1)));
        request.setPrizes(List.of(prize(1, "2000000.00"), prize(2, "1000000.00")));

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.updateTournament(9L, 10L, request);

        assertThat(response.getRounds()).hasSize(1);
        assertThat(response.getRounds().get(0).getName()).isEqualTo("Final Only");
        assertThat(response.getPrizes()).hasSize(2);
        assertThat(response.getPrizes().get(0).getAmount()).isEqualByComparingTo("2000000.00");
    }

    @Test
    void replaceTournamentRoundsUpdatesRoundsAndAudits() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        Tournament tournament = tournament(TournamentStatus.DRAFT);

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.replaceTournamentRounds(9L, 10L,
                List.of(round("Final Only", 1, 1, 2, 8, 1)));

        assertThat(response.getRounds()).hasSize(1);
        assertThat(response.getRounds().get(0).getName()).isEqualTo("Final Only");

        ArgumentCaptor<AdminAuditLog> auditCaptor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(adminAuditLogRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getAction()).isEqualTo("TOURNAMENT_ROUNDS_UPDATED");
    }

    @Test
    void replaceTournamentRoundsRejectsDuplicateRoundOrder() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        Tournament tournament = tournament(TournamentStatus.DRAFT);

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() -> service.replaceTournamentRounds(9L, 10L,
                List.of(
                        round("Qualifier A", 1, 1, 1, 4, 1),
                        round("Qualifier B", 1, 1, 1, 4, 1)
                )))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Round order must be unique within a tournament");
    }

    @Test
    void replaceTournamentRoundsRejectsAdvancementThatCannotFillNextRound() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        Tournament tournament = tournament(TournamentStatus.DRAFT);

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() -> service.replaceTournamentRounds(9L, 10L,
                List.of(
                        round("Qualifier", 1, 1, 1, 4, 1),
                        round("Final", 2, 2, 1, 4, 1)
                )))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Advancement rule cannot fill the next round minimum participants");
    }

    @Test
    void replaceTournamentConfigRejectsNonEditableStatus() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        Tournament tournament = tournament(TournamentStatus.COMPLETED);

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() -> service.replaceTournamentRounds(9L, 10L,
                List.of(round("Final Only", 1, 1, 2, 8, 1))))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only draft or published tournaments can be updated");
    }

    @Test
    void replaceTournamentPrizesUpdatesPrizesAndAudits() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        Tournament tournament = tournament(TournamentStatus.DRAFT);

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.replaceTournamentPrizes(9L, 10L,
                List.of(prize(1, "2000000.00"), prize(2, "1000000.00")));

        assertThat(response.getPrizes()).hasSize(2);
        assertThat(response.getPrizes().get(0).getAmount()).isEqualByComparingTo("2000000.00");

        ArgumentCaptor<AdminAuditLog> auditCaptor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(adminAuditLogRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getAction()).isEqualTo("TOURNAMENT_PRIZES_UPDATED");
    }

    @Test
    void replaceTournamentPrizesRejectsPrizeWithoutAmountOrItem() {
        TournamentServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        Tournament tournament = tournament(TournamentStatus.DRAFT);
        TournamentPrizeRequest request = prize(1, "0.00");

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() -> service.replaceTournamentPrizes(9L, 10L, List.of(request)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Prize must have a positive amount or item name");
    }

    @Test
    void nonAdminCannotUpdateTournamentStatus() {
        TournamentServiceImpl service = service();
        User owner = user(1L, "owner", UserRole.OWNER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> service.updateTournamentStatus(1L, 10L, TournamentStatus.CANCELLED))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Only admins can manage tournaments");
    }

    @Test
    void publicTournamentsUsePublicStatusesOnly() {
        TournamentServiceImpl service = service();
        Tournament openTournament = tournament(TournamentStatus.OPEN_REGISTRATION);
        when(tournamentRepository.findByStatusInOrderByStartAtAsc(any())).thenReturn(List.of(openTournament));

        var response = service.getPublicTournaments();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getStatus()).isEqualTo(TournamentStatus.OPEN_REGISTRATION);

        ArgumentCaptor<List<TournamentStatus>> statuses = ArgumentCaptor.forClass(List.class);
        verify(tournamentRepository).findByStatusInOrderByStartAtAsc(statuses.capture());
        assertThat(statuses.getValue()).contains(TournamentStatus.PUBLISHED, TournamentStatus.OPEN_REGISTRATION);
        assertThat(statuses.getValue()).doesNotContain(TournamentStatus.DRAFT);
    }

    @Test
    void publicTournamentRoundsAndPrizesReturnConfiguredListsForPublicTournament() {
        TournamentServiceImpl service = service();
        Tournament tournament = tournament(TournamentStatus.PUBLISHED);
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));

        var rounds = service.getPublicTournamentRounds(10L);
        var prizes = service.getPublicTournamentPrizes(10L);

        assertThat(rounds).hasSize(2);
        assertThat(rounds.get(0).getRoundOrder()).isEqualTo(1);
        assertThat(prizes).hasSize(1);
        assertThat(prizes.get(0).getRank()).isEqualTo(1);
    }

    @Test
    void publicTournamentRoundsRejectDraftTournamentAsNotFound() {
        TournamentServiceImpl service = service();
        Tournament tournament = tournament(TournamentStatus.DRAFT);
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() -> service.getPublicTournamentRounds(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tournament not found with id: '10'");
    }

    private TournamentServiceImpl service() {
        return new TournamentServiceImpl(tournamentRepository, userRepository, adminAuditLogRepository);
    }

    private TournamentRequest request() {
        LocalDateTime base = LocalDateTime.of(2026, 6, 1, 9, 0);
        TournamentRequest request = new TournamentRequest();
        request.setName("Summer Derby");
        request.setDescription("Phase 6 tournament");
        request.setLocation("Ho Chi Minh City");
        request.setRegistrationOpenAt(base);
        request.setRegistrationCloseAt(base.plusDays(10));
        request.setStartAt(base.plusDays(15));
        request.setEndAt(base.plusDays(16));
        request.setCheckInDeadlineAt(base.plusDays(15).minusHours(2));
        request.setEntryFee(new BigDecimal("100000.00"));
        request.setDepositAmount(new BigDecimal("50000.00"));
        request.setMinTeams(4);
        request.setMaxTeams(16);
        request.setRounds(List.of(round("Qualifier", 1, 4, 1, 4, 2), round("Final", 2, 1, 2, 8, 1)));
        request.setPrizes(List.of(prize(1, "1000000.00"), prize(2, "500000.00")));
        return request;
    }

    private TournamentRoundRequest round(String name, int order, int raceCount,
                                         int minParticipants, int maxParticipants, int advancementCount) {
        TournamentRoundRequest request = new TournamentRoundRequest();
        request.setName(name);
        request.setRoundOrder(order);
        request.setRaceCount(raceCount);
        request.setMinParticipantsPerRace(minParticipants);
        request.setMaxParticipantsPerRace(maxParticipants);
        request.setAdvancementRuleType(AdvancementRuleType.RANK);
        request.setAdvancementCount(advancementCount);
        return request;
    }

    private TournamentPrizeRequest prize(int rank, String amount) {
        TournamentPrizeRequest request = new TournamentPrizeRequest();
        request.setRank(rank);
        request.setAmount(new BigDecimal(amount));
        request.setRecipientPolicy(PrizeRecipientPolicy.OWNER);
        return request;
    }

    private Tournament tournament(TournamentStatus status) {
        Tournament tournament = Tournament.builder()
                .id(10L)
                .name("Summer Derby")
                .location("Ho Chi Minh City")
                .registrationOpenAt(LocalDateTime.of(2026, 6, 1, 9, 0))
                .registrationCloseAt(LocalDateTime.of(2026, 6, 10, 9, 0))
                .startAt(LocalDateTime.of(2026, 6, 15, 9, 0))
                .endAt(LocalDateTime.of(2026, 6, 16, 9, 0))
                .entryFee(new BigDecimal("100000.00"))
                .depositAmount(BigDecimal.ZERO)
                .minTeams(4)
                .maxTeams(16)
                .status(status)
                .build();
        tournament.replaceRounds(List.of(
                TournamentRound.builder()
                        .name("Qualifier")
                        .roundOrder(1)
                        .raceCount(4)
                        .minParticipantsPerRace(1)
                        .maxParticipantsPerRace(4)
                        .advancementRuleType(AdvancementRuleType.RANK)
                        .advancementCount(2)
                        .build(),
                TournamentRound.builder()
                        .name("Final")
                        .roundOrder(2)
                        .raceCount(1)
                        .minParticipantsPerRace(2)
                        .maxParticipantsPerRace(8)
                        .advancementRuleType(AdvancementRuleType.RANK)
                        .advancementCount(1)
                        .build()
        ));
        tournament.replacePrizes(List.of(
                TournamentPrize.builder()
                        .rank(1)
                        .amount(new BigDecimal("1000000.00"))
                        .recipientPolicy(PrizeRecipientPolicy.OWNER)
                        .build()
        ));
        return tournament;
    }

    private User user(Long id, String username, UserRole role) {
        return User.builder()
                .id(id)
                .username(username)
                .email(username + "@example.com")
                .role(role)
                .build();
    }
}
