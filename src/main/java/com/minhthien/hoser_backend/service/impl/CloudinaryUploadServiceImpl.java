package com.minhthien.hoser_backend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.minhthien.hoser_backend.exception.BadRequestException;
import com.minhthien.hoser_backend.service.CloudinaryUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryUploadServiceImpl implements CloudinaryUploadService {
    private final Cloudinary cloudinary;

    @Override
    public String upload(MultipartFile file, String folder) {
        requireFile(file);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto"
            ));
            Object secureUrl = result.get("secure_url");
            if (secureUrl == null || secureUrl.toString().isBlank()) {
                throw new BadRequestException("Cloudinary upload did not return a secure URL");
            }
            return secureUrl.toString();
        } catch (IOException ex) {
            throw new BadRequestException("Failed to upload file");
        }
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        requireContentType(file, true);
        return upload(file, folder);
    }

    @Override
    public String uploadDocument(MultipartFile file, String folder) {
        requireContentType(file, false);
        return upload(file, folder);
    }

    private void requireFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
    }

    private void requireContentType(MultipartFile file, boolean imageOnly) {
        requireFile(file);
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new BadRequestException("File content type is required");
        }
        if (contentType.startsWith("image/")) {
            return;
        }
        if (!imageOnly && "application/pdf".equalsIgnoreCase(contentType)) {
            return;
        }
        throw new BadRequestException(imageOnly
                ? "Only image files are allowed"
                : "Only image or PDF files are allowed");
    }
}
