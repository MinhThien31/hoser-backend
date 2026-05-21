package com.minhthien.hoser_backend.repository;

import com.minhthien.hoser_backend.entity.TournamentPrize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentPrizeRepository extends JpaRepository<TournamentPrize, Long> {
}
