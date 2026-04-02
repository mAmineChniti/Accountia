package com.accountia.expense.service;

import com.accountia.expense.dto.BusinessDTO;
import com.accountia.expense.dto.ClientDTO;
import com.accountia.expense.dto.ExpenseDetailsDTO;
import com.accountia.expense.dto.ExpenseRequest;
import com.accountia.expense.entity.Expense;
import com.accountia.expense.feign.BusinessFeignClient;
import com.accountia.expense.feign.ClientFeignClient;
import com.accountia.expense.repository.ExpenseRepository;
import com.accountia.expense_ms.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
        return expenseRepository.findAll();
    }

    public Optional<Expense> getById(Long id) {
        return expenseRepository.findById(id);
    }

    public List<Expense> getByCategorie(String categorie) {
        return expenseRepository.findByCategorieIgnoreCase(categorie);
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
        return expenseRepository.save(expense);
    }

    public Expense update(Long id, ExpenseRequest request) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Depense introuvable avec id: " + id));

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
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Depense introuvable avec id: " + id);
        }
        expenseRepository.deleteById(id);
    }

    public Optional<ExpenseDetailsDTO> getByIdWithRelations(Long id) {
        try {
            return expenseRepository.findById(id)
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
}
