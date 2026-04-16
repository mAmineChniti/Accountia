package com.accountia.invoice.repository;

import com.accountia.invoice.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Trouver toutes les factures d'un propriétaire
    List<Invoice> findByOwnerSubject(String ownerSubject);

    // Trouver par statut
    List<Invoice> findByStatus(String status);

    // Trouver par propriétaire ET statut
    List<Invoice> findByOwnerSubjectAndStatus(String ownerSubject, String status);

    // Trouver par nom de client (contient)
    List<Invoice> findByClientNameContainingIgnoreCase(String clientName);

    // Trouver par propriétaire ET nom de client
    List<Invoice> findByOwnerSubjectAndClientNameContainingIgnoreCase(String ownerSubject, String clientName);
}
