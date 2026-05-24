package com.minhthien.hoser_backend.repository;

import com.minhthien.hoser_backend.entity.JockeyChallengeResult;
import com.minhthien.hoser_backend.enums.RacePayoutStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JockeyChallengeResultRepository extends JpaRepository<JockeyChallengeResult, Long> {
    @EntityGraph(attributePaths = {"tournament", "jockey"})
    List<JockeyChallengeResult> findByTournamentIdOrderByChallengeRankAsc(Long tournamentId);

    @EntityGraph(attributePaths = {"tournament", "jockey"})
    List<JockeyChallengeResult> findByPayoutStatusOrderByFinalizedAtAscIdAsc(RacePayoutStatus payoutStatus);

    boolean existsByTournamentId(Long tournamentId);
}
