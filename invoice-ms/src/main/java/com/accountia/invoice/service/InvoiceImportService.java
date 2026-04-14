package com.accountia.invoice.service;

import com.accountia.invoice.domain.entity.Invoice;
import com.accountia.invoice.domain.entity.InvoiceLineItem;
import com.accountia.invoice.domain.entity.InvoiceRecipient;
import com.accountia.invoice.domain.enums.InvoiceStatus;
import com.accountia.invoice.domain.enums.RecipientType;
import com.accountia.invoice.dto.response.BulkImportResponse;
import com.accountia.invoice.dto.response.BulkImportResultItem;
import com.accountia.invoice.repository.InvoiceRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles bulk invoice import from CSV and Excel files.
 *
 * <p>Supported formats:
 * <ul>
 *   <li>CSV (.csv) — parsed with OpenCSV</li>
 *   <li>Excel (.xlsx) — parsed with Apache POI</li>
 * </ul>
 *
 * <p>Required columns: invoiceNumber, recipientType, issuedDate, dueDate
 * <br>Optional columns: recipientEmail, recipientDisplayName, productIds,
 *   productNames, quantities, unitPrices, description, paymentTerms, currency
 *
 * <p>The method is NOT @Transactional at the top level so each row can
 * succeed or fail independently. Per-row saves are wrapped in try/catch.
 */
@Service
public class InvoiceImportService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceImportService.class);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceNumberGenerator numberGenerator;

    public InvoiceImportService(InvoiceRepository invoiceRepository,
                                 InvoiceNumberGenerator numberGenerator) {
        this.invoiceRepository = invoiceRepository;
        this.numberGenerator = numberGenerator;
    }

    /**
     * Imports invoices from a CSV or Excel file.
     *
     * <p>Returns a full result report with per-row success/failure details.
     * Partial success is allowed: some rows can fail while others succeed.
     *
     * @param file       uploaded file (CSV or XLSX)
     * @param businessId issuing business UUID (from query param)
     */
    public BulkImportResponse importInvoices(MultipartFile file, String businessId) {
        Instant startTime = Instant.now();

        List<Map<String, String>> rows;
        try {
            // Determine file type by extension or MIME type
            String filename = file.getOriginalFilename() != null
                    ? file.getOriginalFilename().toLowerCase() : "";
            if (filename.endsWith(".csv")) {
                rows = parseCsv(file);
            } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                rows = parseExcel(file);
            } else {
                throw new IllegalArgumentException("Unsupported file format. Use CSV or Excel (.xlsx).");
            }
        } catch (Exception e) {
            log.error("Failed to parse import file: {}", e.getMessage());
            return new BulkImportResponse(0, 0, 0, 0, List.of(), startTime, Instant.now(),
                    java.time.Duration.between(startTime, Instant.now()).toMillis());
        }

        List<BulkImportResultItem> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 1; // 1-based for display
            Map<String, String> row = rows.get(i);
            try {
                String invoiceId = importSingleRow(row, businessId);
                results.add(new BulkImportResultItem(rowNum, true, "Imported successfully", invoiceId, List.of()));
                successCount++;
            } catch (Exception e) {
                log.warn("Row {} import failed: {}", rowNum, e.getMessage());
                results.add(new BulkImportResultItem(rowNum, false, e.getMessage(), null, List.of(e.getMessage())));
                failedCount++;
            }
        }

        Instant endTime = Instant.now();
        long processingMs = java.time.Duration.between(startTime, endTime).toMillis();

        log.info("Import completed: {} success, {} failed in {}ms", successCount, failedCount, processingMs);

        return new BulkImportResponse(
                rows.size(), successCount, failedCount, 0,
                results, startTime, endTime, processingMs
        );
    }

    /**
     * Imports a single row as an invoice. Wrapped in its own transaction so
     * a failure in one row doesn't roll back the others.
     */
    @Transactional
    protected String importSingleRow(Map<String, String> row, String businessId) {
        // Validate required fields
        List<String> errors = new ArrayList<>();

        String recipientTypeStr = row.getOrDefault("recipientType", "").trim();
        String issuedDateStr = row.getOrDefault("issuedDate", "").trim();
        String dueDateStr = row.getOrDefault("dueDate", "").trim();

        if (recipientTypeStr.isBlank()) errors.add("recipientType is required");
        if (issuedDateStr.isBlank()) errors.add("issuedDate is required");
        if (dueDateStr.isBlank()) errors.add("dueDate is required");

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }

        LocalDate issuedDate = LocalDate.parse(issuedDateStr);
        LocalDate dueDate = LocalDate.parse(dueDateStr);

        RecipientType recipientType;
        try {
            recipientType = RecipientType.valueOf(recipientTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid recipientType: " + recipientTypeStr);
        }

        // Check for duplicate invoice number if provided
        String providedNumber = row.getOrDefault("invoiceNumber", "").trim();
        String invoiceNumber;
        if (!providedNumber.isBlank()) {
            if (invoiceRepository.existsByInvoiceNumber(providedNumber)) {
                throw new IllegalArgumentException("Invoice number already exists: " + providedNumber);
            }
            invoiceNumber = providedNumber;
        } else {
            invoiceNumber = numberGenerator.generate(businessId);
        }

        // Build recipient
        InvoiceRecipient recipient = InvoiceRecipient.builder()
                .type(recipientType)
                .email(row.getOrDefault("recipientEmail", null))
                .displayName(row.getOrDefault("recipientDisplayName", null))
                .platformId(row.getOrDefault("recipientPlatformId", null))
                .build();

        // Build a simple line item if product info is provided
        List<InvoiceLineItem> lineItems = new ArrayList<>();
        String productName = row.getOrDefault("productNames", "Imported Item");
        String productId = row.getOrDefault("productIds", "imported");
        BigDecimal quantity = parseBigDecimal(row.get("quantities"), BigDecimal.ONE);
        BigDecimal unitPrice = parseBigDecimal(row.get("unitPrices"), BigDecimal.ZERO);
        BigDecimal amount = quantity.multiply(unitPrice);

        InvoiceLineItem item = InvoiceLineItem.builder()
                .productId(productId)
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .amount(amount)
                .sortOrder(0)
                .build();
        lineItems.add(item);

        // Build and save the invoice
        Invoice invoice = Invoice.builder()
                .issuerBusinessId(businessId)
                .invoiceNumber(invoiceNumber)
                .status(InvoiceStatus.DRAFT)
                .totalAmount(amount)
                .amountPaid(BigDecimal.ZERO)
                .currency(row.getOrDefault("currency", "TND"))
                .issuedDate(issuedDate)
                .dueDate(dueDate)
                .description(row.getOrDefault("description", null))
                .paymentTerms(row.getOrDefault("paymentTerms", null))
                .recipient(recipient)
                .build();

        lineItems.forEach(invoice::addLineItem);
        return invoiceRepository.save(invoice).getId();
    }

    // ── File parsers ──────────────────────────────────────────────────────────

    /** Parses a CSV file into a list of row maps (column name → value). */
    private List<Map<String, String>> parseCsv(MultipartFile file) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> allRows = reader.readAll();
            if (allRows.isEmpty()) return List.of();

            // First row is the header
            String[] headers = allRows.get(0);
            List<Map<String, String>> result = new ArrayList<>();

            for (int i = 1; i < allRows.size(); i++) {
                String[] values = allRows.get(i);
                Map<String, String> row = new java.util.LinkedHashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    row.put(headers[j].trim(), j < values.length ? values[j].trim() : "");
                }
                result.add(row);
            }
            return result;
        }
    }

    /** Parses an Excel (.xlsx) file into a list of row maps. */
    private List<Map<String, String>> parseExcel(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Map<String, String>> result = new ArrayList<>();

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return List.of();

            // Read header names
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellString(cell).trim());
            }

            // Read data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowMap = new java.util.LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    rowMap.put(headers.get(j), cell != null ? getCellString(cell) : "");
                }
                result.add(rowMap);
            }
            return result;
        }
    }

    /** Reads an Excel cell as a string regardless of cell type. */
    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                // Remove trailing .0 from integer-like numbers
                double val = cell.getNumericCellValue();
                yield val == Math.floor(val) ? String.valueOf((long) val) : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private BigDecimal parseBigDecimal(String value, BigDecimal defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
