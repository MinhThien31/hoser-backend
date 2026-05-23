package com.minhthien.hoser_backend.service;

import com.minhthien.hoser_backend.dto.response.EligibleHorseTeamResponse;

import java.util.List;

public interface HorseTeamService {
    List<EligibleHorseTeamResponse> getOwnerEligibleHorseTeams(Long ownerId);

    List<EligibleHorseTeamResponse> getAdminTournamentEligibleHorseTeams(Long adminId, Long tournamentId);
}
