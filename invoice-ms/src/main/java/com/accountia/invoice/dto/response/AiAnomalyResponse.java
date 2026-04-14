package com.accountia.invoice.dto.response;

import java.util.List;

/**
 * Anomaly detection result for an invoice before it is finalized.
 *
 * <p>Returned by {@code POST /invoices/ai/detect-anomalies}.
 * Contains a list of anomalies found, each with a severity level.
 */
public record AiAnomalyResponse(

        String invoiceId,

        /** Whether any anomalies were detected. */
        boolean hasAnomalies,

        /** Ordered list of anomalies (most severe first). */
        List<Anomaly> anomalies,

        /** Overall assessment by AI (null if aiEnhanced=false). */
        String summary,

        /** True when AI enhanced the rule-based results. */
        boolean aiEnhanced
) {

    /**
     * A single detected anomaly.
     *
     * @param severity INFO | WARNING | CRITICAL
     * @param type     e.g. "PRICE_DEVIATION", "DUPLICATE_SUSPECTED", "VAT_INCONSISTENCY"
     * @param message  Human-readable description
     */
    public record Anomaly(String severity, String type, String message) {}
}
