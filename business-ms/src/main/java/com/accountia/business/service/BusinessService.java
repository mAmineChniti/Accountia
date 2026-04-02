package com.accountia.business.service;

import com.accountia.business.dto.BusinessRequest;
import com.accountia.business.dto.ClientDTO;
import com.accountia.business.entity.Business;
import com.accountia.business.feign.ClientFeignClient;
import com.accountia.business.repository.BusinessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.accountia.business.dto.BusinessWithClientsDTO;

@Service
public class BusinessService {

    private static final Logger log = LoggerFactory.getLogger(BusinessService.class);

    private final BusinessRepository businessRepository;
    private final ClientFeignClient clientFeignClient;

    public BusinessService(BusinessRepository businessRepository,
                           ClientFeignClient clientFeignClient) {
        this.businessRepository = businessRepository;
        this.clientFeignClient = clientFeignClient;
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    public List<Business> getAll() {
        return businessRepository.findAll();
    }

    public Optional<Business> getById(Long id) {
        return businessRepository.findById(id);
    }

    public List<Business> searchByNom(String nom) {
        return businessRepository.findByNomContaining(nom);
    }

    public List<Business> getBySecteur(String secteur) {
        return businessRepository.findBySecteurIgnoreCase(secteur);
    }

    public Business create(BusinessRequest request, Long ownerUserId) {
        // Vérifier unicité email
        if (request.getEmail() != null &&
                businessRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un business avec cet email existe déjà");
        }
        // Vérifier unicité SIRET
        if (request.getSiret() != null &&
                businessRepository.findBySiret(request.getSiret()).isPresent()) {
            throw new IllegalArgumentException("Un business avec ce SIRET existe déjà");
        }

        Business b = new Business();
        b.setNom(request.getNom());
        b.setSecteur(request.getSecteur());
        b.setAdresse(request.getAdresse());
        b.setSiret(request.getSiret());
        b.setEmail(request.getEmail());
        b.setTelephone(request.getTelephone());
        b.setOwnerUserId(ownerUserId);

        return businessRepository.save(b);
    }

    public Business update(Long id, BusinessRequest request) {
        Business b = businessRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Business introuvable avec id: " + id));

        if (request.getNom() != null) b.setNom(request.getNom());
        if (request.getSecteur() != null) b.setSecteur(request.getSecteur());
        if (request.getAdresse() != null) b.setAdresse(request.getAdresse());
        if (request.getTelephone() != null) b.setTelephone(request.getTelephone());

        if (request.getEmail() != null && !request.getEmail().equals(b.getEmail())) {
            businessRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                throw new IllegalArgumentException("Email déjà utilisé par un autre business");
            });
            b.setEmail(request.getEmail());
        }

        if (request.getSiret() != null && !request.getSiret().equals(b.getSiret())) {
            businessRepository.findBySiret(request.getSiret()).ifPresent(existing -> {
                throw new IllegalArgumentException("SIRET déjà utilisé par un autre business");
            });
            b.setSiret(request.getSiret());
        }

        return businessRepository.save(b);
    }

    public void delete(Long id) {
        if (!businessRepository.existsById(id)) {
            throw new IllegalArgumentException("Business introuvable avec id: " + id);
        }
        businessRepository.deleteById(id);
    }

    // ─── FEIGN : Appel vers client-ms ────────────────────────────────────────

    /**
     * Récupère tous les clients depuis client-ms via OpenFeign.
     * Utilisé pour lister les clients associés à un business.
     */
    public List<ClientDTO> getClientsFromClientMs(String authHeader) {
        try {
            ResponseEntity<List<ClientDTO>> response = clientFeignClient.getAllClients(authHeader);
            if (response != null && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à client-ms: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Recherche des clients par nom d'entreprise dans client-ms via OpenFeign.
     */
    public List<ClientDTO> searchClientsFromClientMs(String nom, String authHeader) {
        try {
            ResponseEntity<List<ClientDTO>> response =
                    clientFeignClient.searchClientsByNom(nom, authHeader);
            if (response != null && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la recherche dans client-ms: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

        /**
         * Récupère un business par ID avec la liste de ses clients depuis client-ms.
         */
        public BusinessWithClientsDTO getByIdWithClients(Long id, String authHeader) {
            try {
                return businessRepository.findById(id).map(business -> {
                // Récupère les clients liés à ce business via nomEntreprise
                List<ClientDTO> clients = Collections.emptyList();
                try {
                    ResponseEntity<List<ClientDTO>> response = clientFeignClient.searchClientsByNomEntreprise(
                            business.getNom(), authHeader);
                    if (response != null && response.getBody() != null) {
                        clients = response.getBody();
                    }
                } catch (Exception e) {
                    log.error("Erreur lors de la récupération des clients du business: {}", e.getMessage());
                }

                    // Crée le DTO avec les clients
                    return new BusinessWithClientsDTO(
                        business.getId(),
                        business.getNom(),
                        business.getSecteur(),
                        business.getAdresse(),
                        business.getSiret(),
                        business.getEmail(),
                        business.getTelephone(),
                        business.getCreatedAt(),
                        business.getUpdatedAt(),
                        clients
                    );
                }).orElse(null);
            } catch (Exception e) {
                log.error("Erreur lors de la récupération du business avec clients: {}", e.getMessage());
                return null;
            }
        }
}