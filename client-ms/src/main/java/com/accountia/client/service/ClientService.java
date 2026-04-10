package com.accountia.client.service;

import com.accountia.client.entity.Client;
import com.accountia.client.repository.ClientRepository;
import com.accountia.client.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    // GET ALL
    public List<Client> getAllClients() {
        if (SecurityUtil.isAdmin()) {
            return clientRepository.findAll();
        }
        String subject = SecurityUtil.getCurrentSubject();
        if (subject == null || subject.isBlank()) {
            return List.of();
        }
        return clientRepository.findByOwnerSubject(subject);
    }

    // GET BY ID
    public Optional<Client> getClientById(Integer id) {
        return clientRepository.findById(id)
                .filter(this::canAccessClient);
    }

    // GET BY NOM
    public List<Client> getClientsByNom(String nom) {
        if (SecurityUtil.isAdmin()) {
            return clientRepository.findByNomContaining(nom);
        }
        String subject = SecurityUtil.getCurrentSubject();
        if (subject == null || subject.isBlank()) {
            return List.of();
        }
        return clientRepository.findByOwnerSubjectAndNomContaining(subject, nom);
    }

    // GET BY NOM ENTREPRISE
    public List<Client> getClientsByNomEntreprise(String nomEntreprise) {
        if (SecurityUtil.isAdmin()) {
            return clientRepository.findByNomEntrepriseContainingIgnoreCase(nomEntreprise);
        }
        String subject = SecurityUtil.getCurrentSubject();
        if (subject == null || subject.isBlank()) {
            return List.of();
        }
        return clientRepository.findByOwnerSubjectAndNomEntrepriseContainingIgnoreCase(subject, nomEntreprise);
    }

    // CREATE
    public Client createClient(Client client) {
        client.setOwnerSubject(SecurityUtil.getCurrentSubject());
        return clientRepository.save(client);
    }

    // UPDATE
    public Client updateClient(Integer id, Client newClient) {
        return clientRepository.findById(id).map(existing -> {
            ensureOwnershipOrAdmin(existing);
            existing.setNom(newClient.getNom());
            existing.setPrenom(newClient.getPrenom());
            existing.setEmail(newClient.getEmail());
            existing.setTelephone(newClient.getTelephone());
            existing.setAdresse(newClient.getAdresse());
            existing.setNomEntreprise(newClient.getNomEntreprise());
            return clientRepository.save(existing);
        }).orElse(null);
    }

    // DELETE
    public String deleteClient(Integer id) {
        Optional<Client> existing = clientRepository.findById(id);
        if (existing.isPresent()) {
            ensureOwnershipOrAdmin(existing.get());
            clientRepository.deleteById(id);
            return "Client supprimé avec succès";
        }
        return "Client introuvable";
    }

    private void ensureOwnershipOrAdmin(Client client) {
        if (canAccessClient(client)) {
            return;
        }
        throw new AccessDeniedException("Acces refuse: vous ne pouvez modifier que vos propres clients");
    }

    private boolean canAccessClient(Client client) {
        if (SecurityUtil.isAdmin()) {
            return true;
        }
        String subject = SecurityUtil.getCurrentSubject();
        return subject != null && subject.equals(client.getOwnerSubject());
    }
}

