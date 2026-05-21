package com.minhthien.hoser_backend.service;

import com.minhthien.hoser_backend.dto.request.TournamentRequest;
import com.minhthien.hoser_backend.dto.response.TournamentResponse;
import com.minhthien.hoser_backend.enums.TournamentStatus;

import java.util.List;

public interface TournamentService {
    TournamentResponse createTournament(Long adminId, TournamentRequest request);

    TournamentResponse updateTournament(Long adminId, Long tournamentId, TournamentRequest request);

    TournamentResponse publishTournament(Long adminId, Long tournamentId);

    TournamentResponse openRegistration(Long adminId, Long tournamentId);

    List<TournamentResponse> getAdminTournaments(TournamentStatus status);

    TournamentResponse getAdminTournament(Long tournamentId);

    List<TournamentResponse> getPublicTournaments();

    TournamentResponse getPublicTournament(Long tournamentId);
}
