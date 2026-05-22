package com.minhthien.hoser_backend.controller;

import com.minhthien.hoser_backend.dto.request.TournamentRequest;
import com.minhthien.hoser_backend.dto.request.TournamentUpdateRequest;
import com.minhthien.hoser_backend.dto.response.ApiResponse;
import com.minhthien.hoser_backend.dto.response.TournamentResponse;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.TournamentStatus;
import com.minhthien.hoser_backend.service.TournamentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class TournamentController {
    private final TournamentService tournamentService;

    @PostMapping("/admin/tournaments")
    public ResponseEntity<ApiResponse<TournamentResponse>> createTournament(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody TournamentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tournament created",
                tournamentService.createTournament(currentUser.getId(), request)));
    }

    @PutMapping("/admin/tournaments/{id}")
    public ResponseEntity<ApiResponse<TournamentResponse>> updateTournament(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            @Valid @RequestBody TournamentUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tournament updated",
                tournamentService.updateTournament(currentUser.getId(), id, request)));
    }

    @PutMapping("/admin/tournaments/{id}/status")
    public ResponseEntity<ApiResponse<TournamentResponse>> updateTournamentStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            @RequestParam TournamentStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Tournament status updated",
                tournamentService.updateTournamentStatus(currentUser.getId(), id, status)));
    }

    @GetMapping("/admin/tournaments")
    public ResponseEntity<ApiResponse<List<TournamentResponse>>> getAdminTournaments(
            @RequestParam(required = false) TournamentStatus status) {
        return ResponseEntity.ok(ApiResponse.success(tournamentService.getAdminTournaments(status)));
    }

    @GetMapping("/admin/tournaments/{id}")
    public ResponseEntity<ApiResponse<TournamentResponse>> getAdminTournament(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(tournamentService.getAdminTournament(id)));
    }

    @GetMapping("/tournaments")
    public ResponseEntity<ApiResponse<List<TournamentResponse>>> getPublicTournaments() {
        return ResponseEntity.ok(ApiResponse.success(tournamentService.getPublicTournaments()));
    }

    @GetMapping("/tournaments/{id}")
    public ResponseEntity<ApiResponse<TournamentResponse>> getPublicTournament(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(tournamentService.getPublicTournament(id)));
    }
}
