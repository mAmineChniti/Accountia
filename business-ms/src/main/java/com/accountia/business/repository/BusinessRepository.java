package com.accountia.business.repository;

import com.accountia.business.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    @Query("SELECT b FROM Business b WHERE b.nom LIKE %:nom%")
    List<Business> findByNomContaining(@Param("nom") String nom);

    List<Business> findBySecteurIgnoreCase(String secteur);

    // NOTE : ces méthodes retournent Optional<Business>
    // Spring Data les génère automatiquement depuis le nom de la méthode
    Optional<Business> findByEmail(String email);

    Optional<Business> findBySiret(String siret);

    List<Business> findByOwnerUserId(Long ownerUserId);

    List<Business> findByOwnerSubject(String ownerSubject);
}