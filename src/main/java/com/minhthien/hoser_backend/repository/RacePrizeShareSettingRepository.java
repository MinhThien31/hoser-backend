package com.minhthien.hoser_backend.repository;

import com.minhthien.hoser_backend.entity.RacePrizeShareSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RacePrizeShareSettingRepository extends JpaRepository<RacePrizeShareSetting, Long> {
    List<RacePrizeShareSetting> findAllByOrderByRankAsc();

    Optional<RacePrizeShareSetting> findByRank(Integer rank);
}
