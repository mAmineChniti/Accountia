package com.accountia.expense.service;

import com.accountia.expense.exception.BusinessValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
public class LocalStorageService implements StorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "application/pdf",
        "image/jpeg",
        "image/png"
    );

    private final Path receiptsRoot;

    public LocalStorageService(@Value("${accountia.storage.receipts-root}") String receiptsRoot) {
        this.receiptsRoot = Path.of(receiptsRoot);
    }

    @Override
    public String storeReceipt(Long businessId, Long expenseId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("Receipt file is required");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessValidationException("Invalid receipt type. Only PDF, JPG, PNG allowed");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessValidationException("Receipt file exceeds 5MB limit");
        }

        String extension = getExtension(file.getOriginalFilename(), file.getContentType());
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String filename = "receipt-" + expenseId + "-" + timestamp + extension;
        Path businessDir = receiptsRoot.resolve(String.valueOf(businessId));
        try {
            Files.createDirectories(businessDir);
            Files.copy(file.getInputStream(), businessDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessValidationException("Failed to store receipt");
        }
        return "/" + receiptsRoot.resolve(String.valueOf(businessId)).resolve(filename).toString().replace("\\", "/");
    }

    private String getExtension(String originalFilename, String contentType) {
        String ext = StringUtils.getFilenameExtension(originalFilename);
        if (ext != null && !ext.isBlank()) {
            return "." + ext.toLowerCase();
        }
        if ("application/pdf".equals(contentType)) {
            return ".pdf";
        }
        if ("image/jpeg".equals(contentType)) {
            return ".jpg";
        }
        if ("image/png".equals(contentType)) {
            return ".png";
        }
        return "";
    }
}
