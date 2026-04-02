package com.accountia.expense.controller;

import com.accountia.expense.dto.ExpenseDetailsDTO;
import com.accountia.expense.dto.ExpenseRequest;
import com.accountia.expense.entity.Expense;
import com.accountia.expense.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/expense")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("expense-ms up");
    }

    @GetMapping("/expenses")
    public ResponseEntity<List<Expense>> getAll() {
        List<Expense> expenses = expenseService.getAll();
        if (expenses.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/expenses/{id}")
    public ResponseEntity<Expense> getById(@PathVariable Long id) {
        return expenseService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/expenses/{id}/with-relations")
    public ResponseEntity<ExpenseDetailsDTO> getByIdWithRelations(@PathVariable Long id) {
        return expenseService.getByIdWithRelations(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/expenses/categorie/{categorie}")
    public ResponseEntity<List<Expense>> getByCategorie(@PathVariable String categorie) {
        List<Expense> expenses = expenseService.getByCategorie(categorie);
        if (expenses.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(expenses);
    }

    @PostMapping(value = "/expenses", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody ExpenseRequest request) {
        try {
            Expense created = expenseService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/expenses/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        try {
            Expense updated = expenseService.update(id, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            expenseService.delete(id);
            return ResponseEntity.ok("Depense supprimee avec succes");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
