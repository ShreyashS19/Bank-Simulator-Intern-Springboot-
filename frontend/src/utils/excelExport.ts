import * as XLSX from 'xlsx';
import { Transaction } from '@/services/transactionService';

export const exportTransactionsToExcel = (transactions: Transaction[], accountNumber: string) => {
  const worksheetData = transactions.map(transaction => ({
    'Sender Account': transaction.senderAccountNumber,
    'Receiver Account': transaction.receiverAccountNumber,
    'Amount': `₹${Number(transaction.amount).toLocaleString()}`,
    'Transaction Type': transaction.transactionType,
    'Description': transaction.description || '-',
    // 'Status': transaction.status,
    'Date/Time': new Date(transaction.timestamp).toLocaleString()
  }));

  const worksheet = XLSX.utils.json_to_sheet(worksheetData);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Transactions');

  const maxWidth = 20;
  worksheet['!cols'] = [
    { wch: maxWidth },
    { wch: maxWidth },
    { wch: 15 },
    { wch: 15 },
    { wch: 25 },
    { wch: 12 },
    { wch: 20 }
  ];

  const fileName = `transactions_${accountNumber}_${new Date().toISOString().split('T')[0]}.xlsx`;
  XLSX.writeFile(workbook, fileName);
};
