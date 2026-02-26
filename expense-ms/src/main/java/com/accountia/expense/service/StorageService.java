package com.accountia.expense.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String storeReceipt(Long businessId, Long expenseId, MultipartFile file);
}
