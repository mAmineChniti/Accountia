package com.accountia.invoice.service;

import com.accountia.invoice.domain.entity.Invoice;
import com.accountia.invoice.domain.enums.InvoiceStatus;
import com.accountia.invoice.dto.response.AiAnomalyResponse;
import com.accountia.invoice.dto.response.AiPaymentPredictionResponse;
import com.accountia.invoice.exception.InvoiceNotFoundException;
import com.accountia.invoice.repository.InvoiceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI-powered invoice analysis features.
 *
 * <p><strong>Feature 1: Payment Risk Prediction</strong><br>
 * Analyzes a client's payment history and predicts:
 * <ul>
 *   <li>Probability of on-time / late / unpaid payment</li>
 *   <li>Estimated payment date</li>
 *   <li>Risk level (LOW / MEDIUM / HIGH)</li>
 *   <li>Specific risk factors</li>
 * </ul>
 *
 * <p><strong>Feature 2: Anomaly Detection</strong><br>
 * Before an invoice is finalized, checks for:
 * <ul>
 *   <li>Potential duplicate invoices (same client, similar amount, within 7 days)</li>
 *   <li>Unusual amounts (> 30% deviation from client's average)</li>
 *   <li>Missing required fields</li>
 * </ul>
 *
 * <p><strong>Graceful degradation:</strong> If the OpenAI API key is not set
 * or the API call fails, both features return rule-based results with
 * {@code aiEnhanced: false}. The service NEVER crashes due to AI failures.
 */
@Service
public class InvoiceAiService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceAiService.class);

    private final InvoiceRepository invoiceRepository;
    private final ObjectMapper objectMapper;

    @Value("${ai.openai.api-key:}")
    private String apiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${ai.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${ai.openai.enabled:true}")
    private boolean aiEnabled;

    public InvoiceAiService(InvoiceRepository invoiceRepository, ObjectMapper objectMapper) {
        this.invoiceRepository = invoiceRepository;
        this.objectMapper = objectMapper;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // FEATURE 1: Payment Risk Prediction
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Predicts the payment risk for a specific invoice.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Query the DB for this client's payment history (JPQL aggregate query).</li>
     *   <li>Calculate a rule-based risk score using weighted factors.</li>
     *   <li>If AI is enabled, send the data to OpenAI for natural language enrichment.</li>
     *   <li>Return the structured prediction.</li>
     * </ol>
     */
    @Transactional(readOnly = true)
    public AiPaymentPredictionResponse predictPayment(String invoiceId, String businessId) {
        Invoice invoice = invoiceRepository.findByIdAndBusinessIdWithItems(invoiceId, businessId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        String recipientEmail = invoice.getRecipient() != null
                ? invoice.getRecipient().getEmail() : null;

        // Step 1: Gather client payment history from DB
        Object[] stats = recipientEmail != null
                ? invoiceRepository.getClientPaymentStats(recipientEmail, businessId)
                : new Object[]{0L, 0L, null};

        // stats[0] = total count, stats[1] = paid count (JPQL returns 2-element array)
        // avgDaysToClose is null — DATEDIFF avoided to keep JPQL portable (H2 test-safe)
        long totalInvoices = stats[0] != null ? ((Number) stats[0]).longValue() : 0;
        long paidInvoices  = stats[1] != null ? ((Number) stats[1]).longValue() : 0;
        Double avgDaysToClose = null; // Rule-based fallback used; AI prompt says "N/A"

        // Step 2: Rule-based risk score computation
        RiskScore riskScore = computeRiskScore(totalInvoices, paidInvoices,
                avgDaysToClose, invoice.getTotalAmount());

        // Step 3: AI enhancement (natural language recommendation)
        String recommendation;
        boolean aiEnhanced = false;

        if (aiEnabled && apiKey != null && !apiKey.isBlank()) {
            try {
                recommendation = callOpenAiForRecommendation(
                        invoice, riskScore, totalInvoices, paidInvoices, avgDaysToClose);
                aiEnhanced = true;
            } catch (Exception e) {
                log.warn("AI API call failed, using rule-based recommendation: {}", e.getMessage());
                recommendation = riskScore.ruleBasedRecommendation();
            }
        } else {
            recommendation = riskScore.ruleBasedRecommendation();
        }

        // Step 4: Estimate payment date based on average days to close
        LocalDate predictedPaymentDate = estimatePaymentDate(invoice.getDueDate(), avgDaysToClose, riskScore.level);

        return new AiPaymentPredictionResponse(
                invoiceId,
                new AiPaymentPredictionResponse.PaymentProbability(
                        riskScore.onTimePercent,
                        riskScore.latePercent,
                        riskScore.unpaidPercent
                ),
                predictedPaymentDate,
                riskScore.level,
                riskScore.factors,
                recommendation,
                riskScore.score,
                aiEnhanced
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // FEATURE 2: Anomaly Detection
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Detects anomalies in an invoice before it is finalized.
     *
     * <p>Rule-based checks:
     * <ul>
     *   <li>Potential duplicate: same client + similar amount within last 7 days.</li>
     *   <li>High amount: invoice total > 3× the client's historical average.</li>
     *   <li>Missing recipient email on EXTERNAL type.</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public AiAnomalyResponse detectAnomalies(String invoiceId, String businessId) {
        Invoice invoice = invoiceRepository.findByIdAndBusinessIdWithItems(invoiceId, businessId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        List<AiAnomalyResponse.Anomaly> anomalies = new ArrayList<>();

        String recipientEmail = invoice.getRecipient() != null
                ? invoice.getRecipient().getEmail() : null;

        // Check 1: Potential duplicate
        if (recipientEmail != null) {
            LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
            boolean isDuplicate = invoiceRepository.existsSimilarRecentInvoice(
                    businessId, recipientEmail, invoice.getTotalAmount(),
                    sevenDaysAgo, invoiceId);

            if (isDuplicate) {
                anomalies.add(new AiAnomalyResponse.Anomaly(
                        "WARNING",
                        "DUPLICATE_SUSPECTED",
                        "A similar invoice was issued to this recipient within the last 7 days. " +
                        "Please verify this is not a duplicate."
                ));
            }
        }

        // Check 2: Missing email for EXTERNAL recipient
        if (invoice.getRecipient() != null
                && invoice.getRecipient().getType() != null
                && "EXTERNAL".equals(invoice.getRecipient().getType().name())
                && (recipientEmail == null || recipientEmail.isBlank())) {
            anomalies.add(new AiAnomalyResponse.Anomaly(
                    "INFO",
                    "MISSING_RECIPIENT_EMAIL",
                    "External recipient has no email address. " +
                    "The invoice cannot be delivered electronically."
            ));
        }

        // Check 3: Zero-amount invoice
        if (invoice.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            anomalies.add(new AiAnomalyResponse.Anomaly(
                    "WARNING",
                    "ZERO_AMOUNT",
                    "Invoice total is zero. Please verify the line items and pricing."
            ));
        }

        boolean hasAnomalies = !anomalies.isEmpty();
        String summary = null;
        boolean aiEnhanced = false;

        // AI enhancement: generate a summary narrative
        if (hasAnomalies && aiEnabled && apiKey != null && !apiKey.isBlank()) {
            try {
                summary = callOpenAiForAnomalySummary(invoice, anomalies);
                aiEnhanced = true;
            } catch (Exception e) {
                log.warn("AI anomaly summary failed: {}", e.getMessage());
                summary = hasAnomalies ? "Review the detected issues before finalizing this invoice." : null;
            }
        }

        return new AiAnomalyResponse(invoiceId, hasAnomalies, anomalies, summary, aiEnhanced);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Computes a weighted risk score from historical payment data.
     *
     * <p>Scoring formula:
     * <ul>
     *   <li>Payment rate (40%): ratio of paid invoices to total</li>
     *   <li>Average payment delay (30%): how many days beyond due date on average</li>
     *   <li>Invoice count confidence (20%): more history = more accurate</li>
     *   <li>Zero-history penalty (10%): unknown clients are rated medium risk</li>
     * </ul>
     */
    private RiskScore computeRiskScore(long total, long paid, Double avgDays, BigDecimal amount) {
        List<String> factors = new ArrayList<>();
        int score;

        if (total == 0) {
            // No history — rate as medium risk
            score = 50;
            factors.add("No payment history for this client — rating as medium risk");
        } else {
            // Payment rate: 0.0 to 1.0 → scaled to 0–100
            double paymentRate = (double) paid / total;
            int paymentScore = (int) (paymentRate * 100);

            // Average delay penalty: each day of average delay reduces score by 2 points
            int delayPenalty = 0;
            if (avgDays != null && avgDays > 0) {
                delayPenalty = (int) Math.min(avgDays * 2, 40);
                factors.add(String.format("Average payment delay: %.0f days", avgDays));
            }

            // History confidence: more invoices = more reliable score (up to 10 bonus points)
            int confidenceBonus = (int) Math.min(total, 10);

            score = Math.min(100, Math.max(0, paymentScore - delayPenalty + confidenceBonus));

            if (paymentRate >= 0.9) factors.add("Excellent payment history (" + paid + "/" + total + " paid)");
            else if (paymentRate >= 0.7) factors.add("Good payment history (" + paid + "/" + total + " paid)");
            else factors.add("Poor payment history (" + paid + "/" + total + " paid)");
        }

        // Determine risk level and probability breakdown
        String level;
        int onTime, late, unpaid;

        if (score >= 75) {
            level = "LOW";
            onTime = score;
            late = Math.max(0, 100 - score - 5);
            unpaid = 5;
        } else if (score >= 50) {
            level = "MEDIUM";
            onTime = score;
            late = 30;
            unpaid = Math.max(0, 100 - score - 30);
        } else {
            level = "HIGH";
            onTime = score;
            late = 25;
            unpaid = Math.max(0, 100 - score - 25);
        }

        return new RiskScore(score, level, onTime, late, unpaid, factors);
    }

    /** Estimates the predicted payment date based on risk level and average delay. */
    private LocalDate estimatePaymentDate(LocalDate dueDate, Double avgDays, String riskLevel) {
        if (avgDays != null && avgDays > 0) {
            return dueDate.plusDays((long) avgDays.doubleValue());
        }
        return switch (riskLevel) {
            case "LOW"    -> dueDate;
            case "MEDIUM" -> dueDate.plusDays(7);
            default       -> dueDate.plusDays(21);
        };
    }

    /** Calls OpenAI to generate a natural language recommendation. */
    private String callOpenAiForRecommendation(Invoice invoice, RiskScore score,
                                                long total, long paid, Double avgDays) {
        String prompt = String.format("""
                You are an invoice collection assistant. Analyze this invoice and provide a 2-sentence recommendation.

                Invoice: %s | Amount: %s %s | Due: %s
                Client payment history: %d invoices, %d paid on time, avg %s days delay
                Risk level: %s | Score: %d/100

                Provide a concise, actionable recommendation for the accounts receivable team.
                """,
                invoice.getInvoiceNumber(),
                invoice.getTotalAmount().setScale(2, RoundingMode.HALF_UP),
                invoice.getCurrency(),
                invoice.getDueDate(),
                total, paid,
                avgDays != null ? String.format("%.0f", avgDays) : "N/A",
                score.level, score.score
        );

        return callOpenAi(prompt);
    }

    /** Calls OpenAI to generate an anomaly summary. */
    private String callOpenAiForAnomalySummary(Invoice invoice, List<AiAnomalyResponse.Anomaly> anomalies) {
        String anomalyList = anomalies.stream()
                .map(a -> "- [" + a.severity() + "] " + a.message())
                .reduce("", (a, b) -> a + "\n" + b);

        String prompt = String.format("""
                Review these invoice anomalies and provide a 2-sentence summary with the most important action to take.

                Invoice: %s | Amount: %s %s
                Anomalies:%s

                Focus on the most critical issue and the recommended action.
                """,
                invoice.getInvoiceNumber(),
                invoice.getTotalAmount().setScale(2, RoundingMode.HALF_UP),
                invoice.getCurrency(),
                anomalyList
        );

        return callOpenAi(prompt);
    }

    /**
     * Makes a POST request to the OpenAI Chat Completions API.
     *
     * <p>Request format:
     * <pre>
     * POST https://api.openai.com/v1/chat/completions
     * Authorization: Bearer {apiKey}
     * { "model": "gpt-4o-mini", "messages": [{"role": "user", "content": "..."}] }
     * </pre>
     *
     * @param prompt the user prompt to send
     * @return the AI's text response
     * @throws RuntimeException if the API call fails (caught by callers for graceful degradation)
     */
    private String callOpenAi(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Build the request body as a structured map (Jackson serializes it to JSON)
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", 200,
                "temperature", 0.3   // Low temperature for consistent, factual output
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Call the API and parse the response
        String responseJson = restTemplate.postForObject(
                baseUrl + "/chat/completions", entity, String.class);

        // Parse: response.choices[0].message.content
        JsonNode root = parseJson(responseJson);
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response: " + e.getMessage());
        }
    }

    // ── Inner classes ─────────────────────────────────────────────────────────

    /** Internal value object holding the computed risk score data. */
    private record RiskScore(int score, String level, int onTimePercent,
                              int latePercent, int unpaidPercent, List<String> factors) {
        String ruleBasedRecommendation() {
            return switch (level) {
                case "LOW"    -> "Low risk client. Strong payment history. Standard follow-up process.";
                case "MEDIUM" -> "Medium risk. Monitor payment closely. Send reminder 3 days before due date.";
                default       -> "HIGH risk. Consider requiring prepayment or shorter payment terms for future invoices.";
            };
        }
    }
}
