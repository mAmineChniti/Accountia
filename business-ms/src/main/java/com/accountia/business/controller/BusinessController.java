package com.accountia.business.controller;

import com.accountia.business.entity.Business;
import com.accountia.business.repository.BusinessRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final BusinessRepository businessRepository;

    public BusinessController(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    @PostMapping
    public ResponseEntity<Business> create(@RequestBody Business business) {
        return ResponseEntity.ok(businessRepository.save(business));
    }

    @GetMapping
    public List<Business> getAll() {
        return businessRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Business> getById(@PathVariable Long id) {
        Optional<Business> business = businessRepository.findById(id);
        return business.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Business> update(@PathVariable Long id, @RequestBody Business updated) {
        return businessRepository.findById(id)
                .map(b -> {
                    b.setName(updated.getName());
                    b.setAddress(updated.getAddress());
                    b.setTaxNumber(updated.getTaxNumber());
                    b.setCurrency(updated.getCurrency());
                    businessRepository.save(b);
                    return ResponseEntity.ok(b);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (businessRepository.existsById(id)) {
            businessRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
