package com.example.app.service.impl;

import com.example.app.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    @Value("${app.file.product-image-dir}")
    private String productImageDir;

    @Override
    public String storeFile(MultipartFile file, String directory, String customFileName) throws IOException {
        // Tạo thư mục nếu không tồn tại
        Path directoryPath = Paths.get(uploadDir, directory);
        Files.createDirectories(directoryPath);

        // Tạo tên file duy nhất nếu không có tên custom
        String fileName = customFileName;
        if (fileName == null || fileName.isEmpty()) {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            fileName = UUID.randomUUID().toString() + fileExtension;
        }

        // Lưu file
        Path filePath = directoryPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return directory + "/" + fileName;
    }

    @Override
    public String storeProductImage(MultipartFile file, Long productId, String fileName) throws IOException {
        String productDirectory = productImageDir + "/" + productId;
        return storeFile(file, productDirectory, fileName);
    }

    @Override
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(uploadDir, filePath);
        Files.deleteIfExists(path);
    }

    @Override
    public Path getFilePath(String fileName, String directory) {
        return Paths.get(uploadDir, directory, fileName);
    }

    @Override
    public List<String> listProductImages(Long productId) {
        String productDirectory = productImageDir + "/" + productId;
        Path directoryPath = Paths.get(uploadDir, productDirectory);
        File directory = directoryPath.toFile();

        if (!directory.exists() || !directory.isDirectory()) {
            return new ArrayList<>();
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }

        return java.util.Arrays.stream(files)
                .filter(File::isFile)
                .map(file -> productDirectory + "/" + file.getName())
                .collect(Collectors.toList());
    }
}