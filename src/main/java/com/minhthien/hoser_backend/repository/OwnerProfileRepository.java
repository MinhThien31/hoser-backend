package com.minhthien.hoser_backend.repository;

import com.minhthien.hoser_backend.entity.OwnerProfile;
import com.minhthien.hoser_backend.enums.RoleApprovalStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerProfileRepository extends JpaRepository<OwnerProfile, Long> {
    @EntityGraph(attributePaths = "user")
    Optional<OwnerProfile> findByUserId(Long userId);

    @EntityGraph(attributePaths = "user")
    List<OwnerProfile> findByStatusOrderByCreatedAtDesc(RoleApprovalStatus status);

    @EntityGraph(attributePaths = "user")
    List<OwnerProfile> findAllByOrderByCreatedAtDesc();
}
