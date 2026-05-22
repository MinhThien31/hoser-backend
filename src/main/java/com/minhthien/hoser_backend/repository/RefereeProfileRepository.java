package com.minhthien.hoser_backend.repository;

import com.minhthien.hoser_backend.entity.RefereeProfile;
import com.minhthien.hoser_backend.enums.RoleApprovalStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefereeProfileRepository extends JpaRepository<RefereeProfile, Long> {
    @EntityGraph(attributePaths = "user")
    Optional<RefereeProfile> findByUserId(Long userId);

    boolean existsByLicenseNumberAndUserIdNot(String licenseNumber, Long userId);

    @EntityGraph(attributePaths = "user")
    List<RefereeProfile> findByStatusOrderByCreatedAtDesc(RoleApprovalStatus status);

    @EntityGraph(attributePaths = "user")
    List<RefereeProfile> findAllByOrderByCreatedAtDesc();
}
