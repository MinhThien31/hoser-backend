package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.entity.Horse;
import com.minhthien.hoser_backend.entity.JockeyInvitation;
import com.minhthien.hoser_backend.entity.JockeyProfile;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.AssignmentStatus;
import com.minhthien.hoser_backend.enums.HorseStatus;
import com.minhthien.hoser_backend.enums.JockeyStatus;
import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.exception.ResourceNotFoundException;
import com.minhthien.hoser_backend.exception.UnauthorizedException;
import com.minhthien.hoser_backend.repository.JockeyInvitationRepository;
import com.minhthien.hoser_backend.repository.TournamentRepository;
import com.minhthien.hoser_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HorseTeamServiceImplTest {
    @Mock
    private JockeyInvitationRepository jockeyInvitationRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void ownerEligibleHorseTeamsReturnsAcceptedApprovedTeamsOnly() {
        HorseTeamServiceImpl service = service();
        User owner = user(1L, "owner", UserRole.OWNER);
        JockeyInvitation eligible = invitation(20L, owner, HorseStatus.APPROVED, JockeyStatus.APPROVED,
                AssignmentStatus.ACCEPTED);
        JockeyInvitation rejectedHorse = invitation(21L, owner, HorseStatus.REJECTED, JockeyStatus.APPROVED,
                AssignmentStatus.ACCEPTED);
        JockeyInvitation pendingJockey = invitation(22L, owner, HorseStatus.APPROVED, JockeyStatus.PENDING,
                AssignmentStatus.ACCEPTED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(jockeyInvitationRepository.findByOwnerIdAndStatusOrderByCreatedAtDesc(1L, AssignmentStatus.ACCEPTED))
                .thenReturn(List.of(eligible, rejectedHorse, pendingJockey));

        var responses = service.getOwnerEligibleHorseTeams(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getInvitationId()).isEqualTo(20L);
        assertThat(responses.get(0).getHorseName()).isEqualTo("Horse 20");
        assertThat(responses.get(0).getJockeyFullName()).isEqualTo("Jockey Full Name");
        assertThat(responses.get(0).getAcceptedAt()).isEqualTo(eligible.getRespondedAt());
    }

    @Test
    void ownerEligibleHorseTeamsRejectsNonOwner() {
        HorseTeamServiceImpl service = service();
        User user = user(1L, "user", UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.getOwnerEligibleHorseTeams(1L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Only owners can view eligible horse teams");
    }

    @Test
    void adminEligibleHorseTeamsRequiresExistingTournament() {
        HorseTeamServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> service.getAdminTournamentEligibleHorseTeams(9L, 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tournament not found with id: '10'");
    }

    @Test
    void adminEligibleHorseTeamsReturnsAllEligibleTeams() {
        HorseTeamServiceImpl service = service();
        User admin = user(9L, "admin", UserRole.ADMIN);
        User owner = user(1L, "owner", UserRole.OWNER);
        JockeyInvitation eligible = invitation(20L, owner, HorseStatus.APPROVED, JockeyStatus.APPROVED,
                AssignmentStatus.ACCEPTED);

        when(userRepository.findById(9L)).thenReturn(Optional.of(admin));
        when(tournamentRepository.existsById(10L)).thenReturn(true);
        when(jockeyInvitationRepository.findByStatusOrderByCreatedAtDesc(AssignmentStatus.ACCEPTED))
                .thenReturn(List.of(eligible));

        var responses = service.getAdminTournamentEligibleHorseTeams(9L, 10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getInvitationId()).isEqualTo(20L);
    }

    private HorseTeamServiceImpl service() {
        return new HorseTeamServiceImpl(jockeyInvitationRepository, tournamentRepository, userRepository);
    }

    private JockeyInvitation invitation(Long id, User owner, HorseStatus horseStatus, JockeyStatus jockeyStatus,
                                        AssignmentStatus invitationStatus) {
        User jockey = user(2L, "jockey", UserRole.JOCKEY);
        jockey.setFullName("Jockey Full Name");
        Horse horse = Horse.builder()
                .id(id + 100)
                .owner(owner)
                .name("Horse " + id)
                .status(horseStatus)
                .build();
        JockeyProfile profile = JockeyProfile.builder()
                .id(id + 200)
                .user(jockey)
                .licenseNumber("LIC-" + id)
                .status(jockeyStatus)
                .build();
        return JockeyInvitation.builder()
                .id(id)
                .owner(owner)
                .jockey(jockey)
                .horse(horse)
                .jockeyProfile(profile)
                .status(invitationStatus)
                .hirePrice(new BigDecimal("50000.00"))
                .respondedAt(LocalDateTime.of(2026, 6, 10, 9, 0))
                .build();
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
