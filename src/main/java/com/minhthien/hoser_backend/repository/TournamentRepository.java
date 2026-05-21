package com.minhthien.hoser_backend.repository;

import com.minhthien.hoser_backend.entity.Tournament;
import com.minhthien.hoser_backend.enums.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findAllByOrderByCreatedAtDesc();

    List<Tournament> findByStatusOrderByCreatedAtDesc(TournamentStatus status);

    List<Tournament> findByStatusInOrderByStartAtAsc(Collection<TournamentStatus> statuses);
}
