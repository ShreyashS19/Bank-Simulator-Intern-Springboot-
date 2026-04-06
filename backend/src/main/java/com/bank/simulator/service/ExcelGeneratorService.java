package com.bank.simulator.service;

import com.bank.simulator.entity.TransactionEntity;

import java.util.List;

public interface ExcelGeneratorService {
    /**
     * Generates an Excel workbook for a list of transactions.
     * @param transactions list of transactions to include
     * @param title sheet/report title
     * @return byte array of the xlsx file
     */
    byte[] generateTransactionExcel(List<TransactionEntity> transactions, String title);
}
