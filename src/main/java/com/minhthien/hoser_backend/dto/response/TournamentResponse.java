package com.minhthien.hoser_backend.dto.response;

import com.minhthien.hoser_backend.enums.TournamentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TournamentResponse {
    private Long id;
    private String name;
    private String description;
    private String location;
    private LocalDateTime registrationOpenAt;
    private LocalDateTime registrationCloseAt;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime checkInDeadlineAt;
    private BigDecimal entryFee;
    private BigDecimal depositAmount;
    private Integer minTeams;
    private Integer maxTeams;
    private TournamentStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime openedRegistrationAt;
    private List<TournamentRoundResponse> rounds;
    private List<TournamentPrizeResponse> prizes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
