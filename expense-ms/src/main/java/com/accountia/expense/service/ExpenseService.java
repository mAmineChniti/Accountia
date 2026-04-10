package com.accountia.expense.service;

import com.accountia.expense.dto.BusinessDTO;
import com.accountia.expense.dto.ClientDTO;
import com.accountia.expense.dto.ExpenseDetailsDTO;
import com.accountia.expense.dto.ExpenseRequest;
import com.accountia.expense.entity.Expense;
import com.accountia.expense.feign.BusinessFeignClient;
import com.accountia.expense.feign.ClientFeignClient;
import com.accountia.expense.repository.ExpenseRepository;
import com.accountia.expense.util.SecurityUtil;
import com.accountia.expense_ms.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;
    private final BusinessFeignClient businessFeignClient;
    private final ClientFeignClient clientFeignClient;

    public ExpenseService(ExpenseRepository expenseRepository,
                          BusinessFeignClient businessFeignClient,
                          ClientFeignClient clientFeignClient) {
        this.expenseRepository = expenseRepository;
        this.businessFeignClient = businessFeignClient;
        this.clientFeignClient = clientFeignClient;
    }

    public List<Expense> getAll() {
        if (SecurityUtil.isAdmin()) {
            return expenseRepository.findAll();
        }
        String subject = SecurityUtil.getCurrentSubject();
        if (subject == null || subject.isBlank()) {
            return List.of();
        }
        return expenseRepository.findByOwnerSubject(subject);
    }

    public Optional<Expense> getById(Long id) {
        if (SecurityUtil.isAdmin()) {
            return expenseRepository.findById(id);
        }
        String subject = SecurityUtil.getCurrentSubject();
        if (subject == null || subject.isBlank()) {
            return Optional.empty();
        }
        return expenseRepository.findByIdAndOwnerSubject(id, subject);
    }

    public List<Expense> getByCategorie(String categorie) {
        if (SecurityUtil.isAdmin()) {
            return expenseRepository.findByCategorieIgnoreCase(categorie);
        }
        String subject = SecurityUtil.getCurrentSubject();
        if (subject == null || subject.isBlank()) {
            return List.of();
        }
        return expenseRepository.findByCategorieIgnoreCaseAndOwnerSubject(categorie, subject);
    }

    public Expense create(ExpenseRequest request) {
        validateBusinessIfProvided(request.getBusinessId());
        validateClientIfProvided(request.getClientId());

        Expense expense = new Expense();
        expense.setLibelle(request.getLibelle());
        expense.setDescription(request.getDescription());
        expense.setMontant(request.getMontant());
        expense.setCategorie(request.getCategorie());
        expense.setDateDepense(request.getDateDepense());
        expense.setBusinessId(request.getBusinessId());
        expense.setClientId(request.getClientId());
        expense.setOwnerSubject(SecurityUtil.getCurrentSubject());
        return expenseRepository.save(expense);
    }

    public Expense update(Long id, ExpenseRequest request) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Depense introuvable avec id: " + id));

        ensureOwnershipOrAdmin(existing);

        if (request.getLibelle() != null) existing.setLibelle(request.getLibelle());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getMontant() != null) existing.setMontant(request.getMontant());
        if (request.getCategorie() != null) existing.setCategorie(request.getCategorie());
        if (request.getDateDepense() != null) existing.setDateDepense(request.getDateDepense());
        if (request.getBusinessId() != null) {
            validateBusinessExists(request.getBusinessId());
            existing.setBusinessId(request.getBusinessId());
        }
        if (request.getClientId() != null) {
            validateClientExists(request.getClientId());
            existing.setClientId(request.getClientId());
        }

        return expenseRepository.save(existing);
    }

    public void delete(Long id) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Depense introuvable avec id: " + id));
        ensureOwnershipOrAdmin(existing);
        expenseRepository.deleteById(id);
    }

    public Optional<ExpenseDetailsDTO> getByIdWithRelations(Long id) {
        try {
            return getById(id)
                .map(expense -> new ExpenseDetailsDTO(
                    expense,
                    resolveBusiness(expense.getBusinessId()),
                    resolveClient(expense.getClientId())
                ));
        } catch (Exception ex) {
            log.error("Erreur getByIdWithRelations pour expenseId={}: {}", id, ex.getMessage(), ex);
            throw ex;
        }
    }

    private void validateBusinessIfProvided(Long businessId) {
        if (businessId != null) {
            validateBusinessExists(businessId);
        }
    }

    private void validateClientIfProvided(Integer clientId) {
        if (clientId != null) {
            validateClientExists(clientId);
        }
    }

    private void validateBusinessExists(Long businessId) {
        BusinessDTO business = resolveBusiness(businessId);
        if (business == null) {
            throw new ResourceNotFoundException("Business introuvable avec id: " + businessId);
        }
    }

    private void validateClientExists(Integer clientId) {
        ClientDTO client = resolveClient(clientId);
        if (client == null) {
            throw new ResourceNotFoundException("Client introuvable avec id: " + clientId);
        }
    }

    private BusinessDTO resolveBusiness(Long businessId) {
        if (businessId == null) {
            return null;
        }
        try {
            ResponseEntity<BusinessDTO> response = businessFeignClient.getBusinessById(businessId);
            if (response != null && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception ex) {
            log.warn("Echec appel business-ms pour businessId={}: {}", businessId, ex.getMessage());
            return null;
        }
        return null;
    }

    private ClientDTO resolveClient(Integer clientId) {
        if (clientId == null) {
            return null;
        }
        try {
            ResponseEntity<ClientDTO> response = clientFeignClient.getClientById(clientId);
            if (response != null && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception ex) {
            log.warn("Echec appel client-ms pour clientId={}: {}", clientId, ex.getMessage());
            return null;
        }
        return null;
    }

    private void ensureOwnershipOrAdmin(Expense expense) {
        if (canAccessExpense(expense)) {
            return;
        }
        throw new AccessDeniedException("Acces refuse: vous ne pouvez modifier que vos propres depenses");
    }

    private boolean canAccessExpense(Expense expense) {
        if (SecurityUtil.isAdmin()) {
            return true;
        }
        String subject = SecurityUtil.getCurrentSubject();
        return subject != null && subject.equals(expense.getOwnerSubject());
    }
}
