package com.minhthien.hoser_backend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.minhthien.hoser_backend.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryUploadServiceImplTest {
    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Test
    void uploadReturnsSecureUrl() throws IOException {
        CloudinaryUploadServiceImpl service = new CloudinaryUploadServiceImpl(cloudinary);
        MockMultipartFile file = new MockMultipartFile("image", "horse.jpg", "image/jpeg", "img".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), anyMap())).thenReturn(Map.of("secure_url", "https://cdn.example/horse.jpg"));

        assertThat(service.upload(file, "hoser/horses/images")).isEqualTo("https://cdn.example/horse.jpg");
    }

    @Test
    void uploadRejectsEmptyFile() {
        CloudinaryUploadServiceImpl service = new CloudinaryUploadServiceImpl(cloudinary);
        MockMultipartFile file = new MockMultipartFile("image", "horse.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> service.upload(file, "hoser/horses/images"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("File is required");
    }

    @Test
    void uploadImageRejectsNonImageContentType() {
        CloudinaryUploadServiceImpl service = new CloudinaryUploadServiceImpl(cloudinary);
        MockMultipartFile file = new MockMultipartFile("image", "horse.pdf", "application/pdf", "pdf".getBytes());

        assertThatThrownBy(() -> service.uploadImage(file, "hoser/horses/images"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only image files are allowed");
    }

    @Test
    void uploadDocumentAllowsPdf() throws IOException {
        CloudinaryUploadServiceImpl service = new CloudinaryUploadServiceImpl(cloudinary);
        MockMultipartFile file = new MockMultipartFile("document", "vet.pdf", "application/pdf", "pdf".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), anyMap())).thenReturn(Map.of("secure_url", "https://cdn.example/vet.pdf"));

        assertThat(service.uploadDocument(file, "hoser/horses/documents")).isEqualTo("https://cdn.example/vet.pdf");
    }
}
