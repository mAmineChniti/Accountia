package com.accountia.client.service;

import com.accountia.client.entity.Client;
import com.accountia.client.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    // GET ALL
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    // GET BY ID
    public Optional<Client> getClientById(Integer id) {
        return clientRepository.findById(id);
    }

    // GET BY NOM
    public List<Client> getClientsByNom(String nom) {
        return clientRepository.findByNomContaining(nom);
    }

    // GET BY NOM ENTREPRISE
    public List<Client> getClientsByNomEntreprise(String nomEntreprise) {
        return clientRepository.findByNomEntrepriseContainingIgnoreCase(nomEntreprise);
    }

    // CREATE
    public Client createClient(Client client) {
        return clientRepository.save(client);
    }

    // UPDATE
    public Client updateClient(Integer id, Client newClient) {
        return clientRepository.findById(id).map(existing -> {
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
        if (clientRepository.existsById(id)) {
            clientRepository.deleteById(id);
            return "Client supprimé avec succès";
        }
        return "Client introuvable";
    }
}

