package com.accountia.client.repository;

import com.accountia.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Integer> {

    // Recherche par nom
    @Query("SELECT c FROM Client c WHERE c.nom LIKE %:nom%")
    List<Client> findByNomContaining(@Param("nom") String nom);

    // Recherche par email
    Optional<Client> findByEmail(String email);

    // Recherche par entreprise
    List<Client> findByNomEntrepriseContainingIgnoreCase(String nomEntreprise);

    List<Client> findByOwnerSubject(String ownerSubject);

    List<Client> findByOwnerSubjectAndNomContaining(String ownerSubject, String nom);

    List<Client> findByOwnerSubjectAndNomEntrepriseContainingIgnoreCase(String ownerSubject, String nomEntreprise);
}

