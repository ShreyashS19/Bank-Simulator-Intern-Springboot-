package com.bank.simulator.service.impl;

import com.bank.simulator.entity.TransactionEntity;
import com.bank.simulator.service.ExcelGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ExcelGeneratorServiceImpl implements ExcelGeneratorService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    @Override
    public byte[] generateTransactionExcel(List<TransactionEntity> transactions, String title) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");

            // ---- Styles ----
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle amountStyle = createAmountStyle(workbook);
            CellStyle alternateStyle = createAlternateRowStyle(workbook);

            // ---- Title Row ----
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            // ---- Blank Row ----
            sheet.createRow(1);

            // ---- Header Row ----
            String[] headers = {
                "#", "Transaction ID", "Sender Account", "Receiver Account",
                "Amount (₹)", "Type", "Description", "Date & Time"
            };

            Row headerRow = sheet.createRow(2);
            headerRow.setHeightInPoints(20);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ---- Data Rows ----
            int rowNum = 3;
            int sno = 1;
            for (TransactionEntity txn : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.setHeightInPoints(18);

                CellStyle rowStyle = (sno % 2 == 0) ? alternateStyle : dataStyle;

                createCell(row, 0, String.valueOf(sno++), rowStyle);
                createCell(row, 1, txn.getTransactionId() != null ? txn.getTransactionId() : "N/A", rowStyle);
                createCell(row, 2, txn.getSenderAccountNumber(), rowStyle);
                createCell(row, 3, txn.getReceiverAccountNumber(), rowStyle);

                Cell amountCell = row.createCell(4);
                amountCell.setCellValue(txn.getAmount() != null ? txn.getAmount().doubleValue() : 0.0);
                amountCell.setCellStyle(amountStyle);

                createCell(row, 5, txn.getTransactionType() != null ? txn.getTransactionType() : "ONLINE", rowStyle);
                createCell(row, 6, txn.getDescription() != null ? txn.getDescription() : "", rowStyle);
                createCell(row, 7, txn.getCreatedDate() != null ? txn.getCreatedDate().format(DATE_FORMAT) : "", rowStyle);
            }

            // ---- Summary Row ----
            if (!transactions.isEmpty()) {
                row_createSummaryRow(workbook, sheet, transactions, rowNum, amountStyle);
            }

            // ---- Auto-size columns ----
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 3000));
            }

            // ---- Write to bytes ----
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            log.info("Excel generated with {} transactions", transactions.size());
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate Excel: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    private void row_createSummaryRow(XSSFWorkbook workbook, Sheet sheet,
                                       List<TransactionEntity> transactions, int rowNum, CellStyle amountStyle) {
        double total = transactions.stream()
                .mapToDouble(t -> t.getAmount() != null ? t.getAmount().doubleValue() : 0)
                .sum();

        CellStyle summaryStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        summaryStyle.setFont(font);
        summaryStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        summaryStyle.setAlignment(HorizontalAlignment.CENTER);

        Row summaryRow = sheet.createRow(rowNum + 1);
        summaryRow.setHeightInPoints(20);

        Cell labelCell = summaryRow.createCell(0);
        labelCell.setCellValue("Total Transactions: " + transactions.size());
        labelCell.setCellStyle(summaryStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum + 1, rowNum + 1, 0, 3));

        Cell totalCell = summaryRow.createCell(4);
        totalCell.setCellValue(total);
        totalCell.setCellStyle(amountStyle);
    }

    // ---- Style Helpers ----

    private CellStyle createTitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createAlternateRowStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createAmountStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
