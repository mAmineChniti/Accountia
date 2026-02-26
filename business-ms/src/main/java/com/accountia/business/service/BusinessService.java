package com.accountia.business.service;

import com.accountia.business.entity.Business;
import com.accountia.business.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository repository;

    // CREATE
    public Business create(Business business) {
        return repository.save(business);
    }

    // READ ALL
    public List<Business> getAll() {
        return repository.findAll();
    }

    // READ BY ID
    public Business getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));
    }

    // UPDATE
    public Business update(Long id, Business updatedBusiness) {
        Business business = getById(id);

        business.setName(updatedBusiness.getName());
        business.setAddress(updatedBusiness.getAddress());
        business.setTaxNumber(updatedBusiness.getTaxNumber());
        business.setCurrency(updatedBusiness.getCurrency());

        return repository.save(business);
    }

    // DELETE
    public void delete(Long id) {
        repository.deleteById(id);
    }
}

