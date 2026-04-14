package com.accountia.invoice.controller;

import com.accountia.invoice.dto.response.AiAnomalyResponse;
import com.accountia.invoice.dto.response.AiPaymentPredictionResponse;
import com.accountia.invoice.service.InvoiceAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing AI-powered invoice analysis features.
 *
 * <p><strong>Feature 1 — Payment Risk Prediction</strong><br>
 * Analyzes a client's payment history to predict the risk of non-payment.
 * Uses a weighted scoring algorithm (payment rate, delay, history depth) and
 * optionally enriches the result with an OpenAI-generated natural language recommendation.
 *
 * <p><strong>Feature 2 — Anomaly Detection</strong><br>
 * Runs a set of rule-based checks before an invoice is finalized:
 * duplicate detection, zero-amount check, missing recipient email.
 * Optionally generates an AI summary of the detected issues.
 *
 * <p><strong>Graceful degradation:</strong> If the OpenAI API key is not configured
 * or the API call fails, both features return rule-based results with
 * {@code aiEnhanced: false}. The service NEVER crashes due to AI failures.
 */
@RestController
@RequestMapping("/invoices")
@Tag(name = "AI Features", description = "AI-powered payment prediction and anomaly detection")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceAiController {

    private final InvoiceAiService aiService;

    public InvoiceAiController(InvoiceAiService aiService) {
        this.aiService = aiService;
    }

    // ── Feature 1: Payment Risk Prediction ───────────────────────────────────

    /**
     * Predicts payment risk for a specific invoice.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Queries the DB for the recipient's payment history (total invoices,
     *       paid invoices, average days to close).</li>
     *   <li>Computes a weighted risk score (0–100).</li>
     *   <li>If AI is enabled and the API key is set, calls OpenAI for a natural
     *       language recommendation (max 200 tokens, temperature 0.3).</li>
     *   <li>Returns structured prediction with risk level, probabilities, and
     *       estimated payment date.</li>
     * </ol>
     *
     * <p>Response fields:
     * <ul>
     *   <li>{@code riskLevel}: LOW / MEDIUM / HIGH</li>
     *   <li>{@code score}: 0–100 (higher = safer)</li>
     *   <li>{@code paymentProbability}: { onTimePercent, latePercent, unpaidPercent }</li>
     *   <li>{@code predictedPaymentDate}: estimated date of payment</li>
     *   <li>{@code riskFactors}: list of contributing factors</li>
     *   <li>{@code recommendation}: actionable advice (AI or rule-based)</li>
     *   <li>{@code aiEnhanced}: true if OpenAI was used, false if rule-based fallback</li>
     * </ul>
     *
     * @param id         the invoice UUID to analyze
     * @param businessId the issuer business UUID (authorization guard)
     */
    @Operation(
            summary = "Predict payment risk for an invoice",
            description = "Analyzes the recipient's payment history and predicts the likelihood " +
                          "of on-time, late, or missed payment. Returns a risk score (0-100), risk level " +
                          "(LOW/MEDIUM/HIGH), probability breakdown, and an actionable recommendation. " +
                          "If OpenAI is configured, the recommendation is AI-generated; otherwise " +
                          "a rule-based fallback is used (aiEnhanced: false)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment prediction result",
                    content = @Content(schema = @Schema(implementation = AiPaymentPredictionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @GetMapping("/issued/{id}/ai/payment-prediction")
    public ResponseEntity<AiPaymentPredictionResponse> predictPayment(
            @Parameter(description = "Invoice UUID") @PathVariable String id,
            @Parameter(description = "Business UUID", required = true) @RequestParam String businessId) {
        return ResponseEntity.ok(aiService.predictPayment(id, businessId));
    }

    // ── Feature 2: Anomaly Detection ──────────────────────────────────────────

    /**
     * Detects anomalies in an invoice before it is finalized.
     *
     * <p>Rule-based checks performed:
     * <ul>
     *   <li><strong>DUPLICATE_SUSPECTED</strong>: A similar invoice (same recipient, similar
     *       amount ±10%) was issued within the last 7 days.</li>
     *   <li><strong>MISSING_RECIPIENT_EMAIL</strong>: EXTERNAL recipient has no email address,
     *       so electronic delivery is impossible.</li>
     *   <li><strong>ZERO_AMOUNT</strong>: Invoice total is zero — likely a data entry error.</li>
     * </ul>
     *
     * <p>If anomalies are found and OpenAI is configured, an AI-generated summary
     * is included in the response with the most critical action to take.
     *
     * <p>Response fields:
     * <ul>
     *   <li>{@code hasAnomalies}: true if at least one anomaly was detected</li>
     *   <li>{@code anomalies}: list of { severity, type, message }</li>
     *   <li>{@code summary}: AI or fallback text summary (null if no anomalies)</li>
     *   <li>{@code aiEnhanced}: true if OpenAI generated the summary</li>
     * </ul>
     *
     * @param id         the invoice UUID to check
     * @param businessId the issuer business UUID (authorization guard)
     */
    @Operation(
            summary = "Detect anomalies in an invoice",
            description = "Runs rule-based anomaly checks on an invoice before it is finalized. " +
                          "Detects potential duplicates (same recipient + similar amount within 7 days), " +
                          "missing recipient email for external invoices, and zero-amount invoices. " +
                          "If anomalies are found and OpenAI is configured, returns an AI-generated " +
                          "summary with the recommended action."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anomaly detection result",
                    content = @Content(schema = @Schema(implementation = AiAnomalyResponse.class))),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @GetMapping("/issued/{id}/ai/anomalies")
    public ResponseEntity<AiAnomalyResponse> detectAnomalies(
            @Parameter(description = "Invoice UUID") @PathVariable String id,
            @Parameter(description = "Business UUID", required = true) @RequestParam String businessId) {
        return ResponseEntity.ok(aiService.detectAnomalies(id, businessId));
    }
}
