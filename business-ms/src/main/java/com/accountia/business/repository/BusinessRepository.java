package com.accountia.business.repository;

import com.accountia.business.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, String> {
    Optional<Business> findByTaxId(String taxId);
}
