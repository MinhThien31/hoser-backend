package com.minhthien.hoser_backend.repository;

import com.minhthien.hoser_backend.entity.JockeyInvitation;
import com.minhthien.hoser_backend.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface JockeyInvitationRepository extends JpaRepository<JockeyInvitation, Long> {
    @EntityGraph(attributePaths = {"owner", "jockey", "horse", "jockeyProfile", "jockeyProfile.user"})
    List<JockeyInvitation> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"owner", "jockey", "horse", "jockeyProfile", "jockeyProfile.user"})
    List<JockeyInvitation> findByJockeyIdOrderByCreatedAtDesc(Long jockeyId);

    boolean existsByHorseIdAndJockeyIdAndStatusIn(
            Long horseId,
            Long jockeyId,
            Collection<AssignmentStatus> statuses
    );
}
