package com.minhthien.hoser_backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryUploadService {
    String upload(MultipartFile file, String folder);

    String uploadImage(MultipartFile file, String folder);

    String uploadDocument(MultipartFile file, String folder);
}
