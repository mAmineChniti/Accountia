package com.accountia.invoice.repository;

import com.accountia.invoice.domain.entity.RecurringInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Data access for recurring invoice schedules. */
@Repository
public interface RecurringInvoiceRepository extends JpaRepository<RecurringInvoice, String> {

    Page<RecurringInvoice> findByBusinessId(String businessId, Pageable pageable);

    long countByBusinessId(String businessId);

    Optional<RecurringInvoice> findByIdAndBusinessId(String id, String businessId);

    /**
     * Finds active schedules whose next run date is in the past (due for execution).
     * Called by the recurring invoice scheduler.
     */
    @Query("""
            SELECT r FROM RecurringInvoice r
            WHERE r.status = 'active'
              AND r.nextRunAt <= :now
            """)
    List<RecurringInvoice> findDueSchedules(@Param("now") Instant now);
}
