package com.accountia.business.service;

import com.accountia.business.dto.BusinessSummaryDTO;
import com.accountia.business.model.Business;
import com.accountia.business.repository.BusinessRepository;
import com.accountia.business_ms.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessService {

    private final BusinessRepository businessRepository;

    @Transactional(readOnly = true)
    public BusinessSummaryDTO getBusinessSummary(String id) {
        log.info("Fetching business summary for id: {}", id);
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with id: " + id));

        return BusinessSummaryDTO.builder()
                .id(business.getId())
                .name(business.getName())
                .active(business.isActive())
                .build();
    }
}
