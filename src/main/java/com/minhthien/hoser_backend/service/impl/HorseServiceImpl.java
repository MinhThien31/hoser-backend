package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.dto.request.AdminReviewRequest;
import com.minhthien.hoser_backend.dto.request.HorseRequest;
import com.minhthien.hoser_backend.dto.response.HorseResponse;
import com.minhthien.hoser_backend.entity.Horse;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.HorseStatus;
import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.exception.BadRequestException;
import com.minhthien.hoser_backend.exception.ResourceNotFoundException;
import com.minhthien.hoser_backend.exception.UnauthorizedException;
import com.minhthien.hoser_backend.repository.HorseRepository;
import com.minhthien.hoser_backend.repository.UserRepository;
import com.minhthien.hoser_backend.service.CloudinaryUploadService;
import com.minhthien.hoser_backend.service.HorseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HorseServiceImpl implements HorseService {
    private static final String HORSE_IMAGE_FOLDER = "hoser/horses/images";
    private static final String HORSE_DOCUMENT_FOLDER = "hoser/horses/documents";

    private final HorseRepository horseRepository;
    private final UserRepository userRepository;
    private final CloudinaryUploadService cloudinaryUploadService;

    @Override
    @Transactional
    public HorseResponse createHorse(Long ownerId, HorseRequest request, MultipartFile image, MultipartFile document) {
        User owner = requireUser(ownerId);
        requireRole(owner, UserRole.OWNER, "Only owners can manage horses");

        Horse horse = Horse.builder()
                .owner(owner)
                .status(HorseStatus.PENDING)
                .createdBy(owner.getUsername())
                .updatedBy(owner.getUsername())
                .build();
        applyRequest(horse, request);
        applyUploadedFiles(horse, image, document);
        return mapToResponse(horseRepository.save(horse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorseResponse> getApprovedHorses() {
        return horseRepository.findByStatusOrderByCreatedAtDesc(HorseStatus.APPROVED).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorseResponse> getOwnerHorses(Long ownerId) {
        User owner = requireUser(ownerId);
        requireRole(owner, UserRole.OWNER, "Only owners can view owner horses");
        return horseRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HorseResponse getOwnerHorse(Long ownerId, Long horseId) {
        User owner = requireUser(ownerId);
        requireRole(owner, UserRole.OWNER, "Only owners can view owner horses");
        return horseRepository.findByIdAndOwnerId(horseId, ownerId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Horse", "id", horseId));
    }

    @Override
    @Transactional
    public HorseResponse updateHorse(Long ownerId, Long horseId, HorseRequest request,
                                     MultipartFile image, MultipartFile document) {
        User owner = requireUser(ownerId);
        requireRole(owner, UserRole.OWNER, "Only owners can manage horses");
        Horse horse = horseRepository.findByIdAndOwnerId(horseId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Horse", "id", horseId));

        if (horse.getStatus() == HorseStatus.APPROVED || horse.getStatus() == HorseStatus.SUSPENDED) {
            throw new BadRequestException("Approved or suspended horses cannot be updated by owner");
        }

        applyRequest(horse, request);
        applyUploadedFiles(horse, image, document);
        horse.setStatus(HorseStatus.PENDING);
        horse.setReviewReason(null);
        horse.setReviewedBy(null);
        horse.setReviewedAt(null);
        horse.setUpdatedBy(owner.getUsername());
        return mapToResponse(horseRepository.save(horse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorseResponse> getAdminHorses(HorseStatus status) {
        List<Horse> horses = status == null
                ? horseRepository.findAllByOrderByCreatedAtDesc()
                : horseRepository.findByStatusOrderByCreatedAtDesc(status);
        return horses.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public HorseResponse approveHorse(Long horseId, Long adminId) {
        User admin = requireUser(adminId);
        requireRole(admin, UserRole.ADMIN, "Only admins can review horses");
        Horse horse = requireHorse(horseId);
        review(horse, HorseStatus.APPROVED, admin, null);
        return mapToResponse(horseRepository.save(horse));
    }

    @Override
    @Transactional
    public HorseResponse rejectHorse(Long horseId, Long adminId, AdminReviewRequest request) {
        User admin = requireUser(adminId);
        requireRole(admin, UserRole.ADMIN, "Only admins can review horses");
        Horse horse = requireHorse(horseId);
        review(horse, HorseStatus.REJECTED, admin, requireReason(request));
        return mapToResponse(horseRepository.save(horse));
    }

    @Override
    @Transactional
    public HorseResponse suspendHorse(Long horseId, Long adminId, AdminReviewRequest request) {
        User admin = requireUser(adminId);
        requireRole(admin, UserRole.ADMIN, "Only admins can review horses");
        Horse horse = requireHorse(horseId);
        review(horse, HorseStatus.SUSPENDED, admin, requireReason(request));
        return mapToResponse(horseRepository.save(horse));
    }

    private void applyRequest(Horse horse, HorseRequest request) {
        horse.setName(request.getName());
        horse.setBreed(request.getBreed());
        horse.setAge(request.getAge());
        horse.setGender(request.getGender());
        horse.setColor(request.getColor());
        horse.setHeightCm(request.getHeightCm());
        horse.setWeightKg(request.getWeightKg());
    }

    private void applyUploadedFiles(Horse horse, MultipartFile image, MultipartFile document) {
        if (image != null) {
            horse.setImageUrl(cloudinaryUploadService.uploadImage(image, HORSE_IMAGE_FOLDER));
        }
        if (document != null) {
            horse.setDocumentUrl(cloudinaryUploadService.uploadDocument(document, HORSE_DOCUMENT_FOLDER));
        }
    }

    private void review(Horse horse, HorseStatus status, User admin, String reason) {
        horse.setStatus(status);
        horse.setReviewReason(reason);
        horse.setReviewedBy(admin.getId());
        horse.setReviewedAt(LocalDateTime.now());
        horse.setUpdatedBy(admin.getUsername());
    }

    private String requireReason(AdminReviewRequest request) {
        if (request == null || request.getReason() == null || request.getReason().isBlank()) {
            throw new BadRequestException("Review reason is required");
        }
        return request.getReason();
    }

    private Horse requireHorse(Long horseId) {
        return horseRepository.findById(horseId)
                .orElseThrow(() -> new ResourceNotFoundException("Horse", "id", horseId));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private void requireRole(User user, UserRole role, String message) {
        if (user.getRole() != role) {
            throw new UnauthorizedException(message);
        }
    }

    private HorseResponse mapToResponse(Horse horse) {
        return HorseResponse.builder()
                .id(horse.getId())
                .ownerId(horse.getOwner().getId())
                .ownerUsername(horse.getOwner().getUsername())
                .name(horse.getName())
                .breed(horse.getBreed())
                .age(horse.getAge())
                .gender(horse.getGender())
                .color(horse.getColor())
                .heightCm(horse.getHeightCm())
                .weightKg(horse.getWeightKg())
                .imageUrl(horse.getImageUrl())
                .documentUrl(horse.getDocumentUrl())
                .status(horse.getStatus())
                .reviewReason(horse.getReviewReason())
                .reviewedBy(horse.getReviewedBy())
                .reviewedAt(horse.getReviewedAt())
                .createdAt(horse.getCreatedAt())
                .updatedAt(horse.getUpdatedAt())
                .build();
    }
}
