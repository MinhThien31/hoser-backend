package com.minhthien.hoser_backend.service;

import com.minhthien.hoser_backend.dto.request.TournamentRequest;
import com.minhthien.hoser_backend.dto.request.TournamentPrizeRequest;
import com.minhthien.hoser_backend.dto.request.TournamentRoundRequest;
import com.minhthien.hoser_backend.dto.request.TournamentUpdateRequest;
import com.minhthien.hoser_backend.dto.response.TournamentPrizeResponse;
import com.minhthien.hoser_backend.dto.response.TournamentResponse;
import com.minhthien.hoser_backend.dto.response.TournamentRoundResponse;
import com.minhthien.hoser_backend.enums.TournamentStatus;

import java.util.List;

public interface TournamentService {
    TournamentResponse createTournament(Long adminId, TournamentRequest request);

    TournamentResponse updateTournament(Long adminId, Long tournamentId, TournamentUpdateRequest request);

    TournamentResponse replaceTournamentRounds(Long adminId, Long tournamentId, List<TournamentRoundRequest> requests);

    TournamentResponse replaceTournamentPrizes(Long adminId, Long tournamentId, List<TournamentPrizeRequest> requests);

    TournamentResponse openRegistration(Long adminId, Long tournamentId);

    TournamentResponse closeRegistration(Long adminId, Long tournamentId);

    TournamentResponse updateTournamentStatus(Long adminId, Long tournamentId, TournamentStatus status);

    List<TournamentResponse> getAdminTournaments(TournamentStatus status);

    TournamentResponse getAdminTournament(Long tournamentId);

    List<TournamentResponse> getPublicTournaments();

    TournamentResponse getPublicTournament(Long tournamentId);

    List<TournamentRoundResponse> getPublicTournamentRounds(Long tournamentId);

    List<TournamentPrizeResponse> getPublicTournamentPrizes(Long tournamentId);
}
