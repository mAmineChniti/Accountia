package com.accountia.business.dto;

/**
 * DTO représentant un Client reçu depuis client-ms via OpenFeign.
 * Correspond à l'entité Client du client-ms.
 */
public class ClientDTO {

    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String nomEntreprise;

    public ClientDTO() {}

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getNomEntreprise() { return nomEntreprise; }
    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }
}