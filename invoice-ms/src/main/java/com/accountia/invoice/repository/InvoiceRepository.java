package com.accountia.invoice.repository;

import com.accountia.invoice.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByClientId(Long clientId);

    List<Invoice> findByBusinessId(Long businessId);

    List<Invoice> findByStatus(Invoice.InvoiceStatus status);

    List<Invoice> findByClientNameContainingIgnoreCase(String clientName);
}
