package com.example.app.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileStorageService {
    String storeFile(MultipartFile file, String directory, String customFileName) throws IOException;
    String storeProductImage(MultipartFile file, Long productId, String fileName) throws IOException;
    void deleteFile(String filePath) throws IOException;
    Path getFilePath(String fileName, String directory);
    List<String> listProductImages(Long productId);
}