package com.accountia.business.dto;

import java.time.Instant;
import java.util.List;

public class BusinessWithClientsDTO {

    private Long id;
    private String nom;
    private String secteur;
    private String adresse;
    private String siret;
    private String email;
    private String telephone;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ClientDTO> clients;

    // Constructeur
    public BusinessWithClientsDTO() {}

    public BusinessWithClientsDTO(Long id, String nom, String secteur, String adresse,
                                   String siret, String email, String telephone,
                                   Instant createdAt, Instant updatedAt, List<ClientDTO> clients) {
        this.id = id;
        this.nom = nom;
        this.secteur = secteur;
        this.adresse = adresse;
        this.siret = siret;
        this.email = email;
        this.telephone = telephone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.clients = clients;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getSecteur() { return secteur; }
    public void setSecteur(String secteur) { this.secteur = secteur; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getSiret() { return siret; }
    public void setSiret(String siret) { this.siret = siret; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<ClientDTO> getClients() { return clients; }
    public void setClients(List<ClientDTO> clients) { this.clients = clients; }
}
