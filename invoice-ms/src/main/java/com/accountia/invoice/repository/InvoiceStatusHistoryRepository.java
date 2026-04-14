package com.accountia.invoice.repository;

import com.accountia.invoice.domain.entity.InvoiceStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Data access for the status audit trail. Append-only by design. */
@Repository
public interface InvoiceStatusHistoryRepository extends JpaRepository<InvoiceStatusHistory, Long> {
    List<InvoiceStatusHistory> findByInvoiceIdOrderByChangedAtAsc(String invoiceId);
}
