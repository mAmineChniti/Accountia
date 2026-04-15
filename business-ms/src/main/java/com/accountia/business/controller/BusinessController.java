package com.accountia.business.controller;

import com.accountia.business.dto.BusinessRequest;
import com.accountia.business.dto.BusinessWithClientsDTO;
import com.accountia.business.dto.ClientDTO;
import com.accountia.business.entity.Business;
import com.accountia.business.service.BusinessService;
import com.accountia.business.util.SecurityUtil;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/business")
@CrossOrigin(origins = "*")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("business-ms up");
    }

    /**
     * GET /api/business/businesses
     * Lister tous les businesses
     */
    @GetMapping("/businesses")
    public ResponseEntity<List<Business>> getAll() {
        List<Business> list = businessService.getAll();
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/business/businesses/{id}
     * Récupérer un business par ID
     */
    @GetMapping("/businesses/{id}")
    public ResponseEntity<Business> getById(@PathVariable Long id) {
        return businessService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/business/businesses/{id}/with-clients
     * Récupérer un business par ID avec ses clients
     */
    @GetMapping("/businesses/{id}/with-clients")
    public ResponseEntity<BusinessWithClientsDTO> getByIdWithClients(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        BusinessWithClientsDTO business = businessService.getByIdWithClients(id, authHeader);
        if (business == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(business);
    }

    /**
     * GET /api/business/businesses/search?nom=Tech
     * Recherche par nom
     */
    @GetMapping("/businesses/search")
    public ResponseEntity<List<Business>> searchByNom(@RequestParam String nom) {
        List<Business> list = businessService.searchByNom(nom);
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/business/businesses/secteur/{secteur}
     * Filtrer par secteur
     */
    @GetMapping("/businesses/secteur/{secteur}")
    public ResponseEntity<List<Business>> getBySecteur(@PathVariable String secteur) {
        List<Business> list = businessService.getBySecteur(secteur);
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    /**
     * POST /api/business/businesses
     * Créer un nouveau business
     */
    @PostMapping(value = "/businesses", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody BusinessRequest request) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            Business created = businessService.create(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * PUT /api/business/businesses/{id}
     * Modifier un business
     */
    @PutMapping("/businesses/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody BusinessRequest request) {
        try {
            Business updated = businessService.update(id, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * DELETE /api/business/businesses/{id}
     * Supprimer un business
     */
    @DeleteMapping("/businesses/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            businessService.delete(id);
            return ResponseEntity.ok("Business supprimé avec succès");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET /api/business/clients
     * Récupère tous les clients depuis client-ms via OpenFeign.
     */
    @GetMapping("/clients")
    public ResponseEntity<List<ClientDTO>> getClientsFromClientMs(
            @RequestHeader("Authorization") String authHeader) {
        List<ClientDTO> clients = businessService.getClientsFromClientMs(authHeader);
        if (clients.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clients);
    }

    /**
     * GET /api/business/clients/search?nom=Ben
     * Recherche des clients dans client-ms via OpenFeign.
     */
    @GetMapping("/clients/search")
    public ResponseEntity<List<ClientDTO>> searchClientsFromClientMs(
            @RequestParam String nom,
            @RequestHeader("Authorization") String authHeader) {
        List<ClientDTO> clients = businessService.searchClientsFromClientMs(nom, authHeader);
        if (clients.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clients);
    }
}