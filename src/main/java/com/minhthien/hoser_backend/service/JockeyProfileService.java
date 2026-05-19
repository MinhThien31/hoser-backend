package com.minhthien.hoser_backend.service;

import com.minhthien.hoser_backend.dto.request.AdminReviewRequest;
import com.minhthien.hoser_backend.dto.request.JockeyProfileRequest;
import com.minhthien.hoser_backend.dto.response.JockeyProfileResponse;
import com.minhthien.hoser_backend.enums.JockeyStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface JockeyProfileService {
    JockeyProfileResponse getMyProfile(Long jockeyId);

    JockeyProfileResponse createMyProfile(Long jockeyId, JockeyProfileRequest request,
                                          MultipartFile avatar, MultipartFile licenseDocument);

    JockeyProfileResponse updateMyProfile(Long jockeyId, JockeyProfileRequest request,
                                          MultipartFile avatar, MultipartFile licenseDocument);

    List<JockeyProfileResponse> getAvailableJockeys();

    JockeyProfileResponse getApprovedJockeyProfile(Long jockeyId);

    List<JockeyProfileResponse> getAdminJockeyProfiles(JockeyStatus status);

    JockeyProfileResponse approveJockeyProfile(Long profileId, Long adminId);

    JockeyProfileResponse rejectJockeyProfile(Long profileId, Long adminId, AdminReviewRequest request);

    JockeyProfileResponse suspendJockeyProfile(Long profileId, Long adminId, AdminReviewRequest request);
}
