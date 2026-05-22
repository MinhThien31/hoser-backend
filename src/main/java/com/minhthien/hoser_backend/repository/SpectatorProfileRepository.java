package com.minhthien.hoser_backend.repository;

import com.minhthien.hoser_backend.entity.SpectatorProfile;
import com.minhthien.hoser_backend.enums.RoleApprovalStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpectatorProfileRepository extends JpaRepository<SpectatorProfile, Long> {
    @EntityGraph(attributePaths = "user")
    Optional<SpectatorProfile> findByUserId(Long userId);

    @EntityGraph(attributePaths = "user")
    List<SpectatorProfile> findByStatusOrderByCreatedAtDesc(RoleApprovalStatus status);

    @EntityGraph(attributePaths = "user")
    List<SpectatorProfile> findAllByOrderByCreatedAtDesc();
}
