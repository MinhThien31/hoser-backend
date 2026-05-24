package com.minhthien.hoser_backend.repository;

import com.minhthien.hoser_backend.entity.Race;
import com.minhthien.hoser_backend.enums.RaceStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {
    @EntityGraph(attributePaths = {"tournament", "referee", "prizes", "participants"})
    List<Race> findByTournamentIdOrderByScheduledStartAtAsc(Long tournamentId);

    List<Race> findByRefereeIdOrderByScheduledStartAtAsc(Long refereeId);

    List<Race> findByTournamentIdAndStatusIn(Long tournamentId, Collection<RaceStatus> statuses);

    boolean existsByRefereeIdAndScheduledStartAtLessThanAndScheduledEndAtGreaterThan(
            Long refereeId,
            LocalDateTime scheduledEndAt,
            LocalDateTime scheduledStartAt
    );
}
