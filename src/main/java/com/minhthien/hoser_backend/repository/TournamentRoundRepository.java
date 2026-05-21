package com.minhthien.hoser_backend.repository;

import com.minhthien.hoser_backend.entity.TournamentRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentRoundRepository extends JpaRepository<TournamentRound, Long> {
}
