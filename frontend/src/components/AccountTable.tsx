import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Eye, Edit, Trash2 } from "lucide-react";
import { Account } from "@/services/accountService"; 
import { motion } from "framer-motion";

interface AccountTableProps {
  accounts: Account[];
  onView: (account: Account) => void;
  onEdit: (account: Account) => void;
  onDelete: (account: Account) => void;
}

export const AccountTable = ({ accounts, onView, onEdit, onDelete }: AccountTableProps) => {
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR'
    }).format(amount);
  };

  return (
    <div className="rounded-md border overflow-x-auto">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Account Number</TableHead>
            <TableHead>Name on Account</TableHead>
            <TableHead>Bank Name</TableHead>
            <TableHead>IFSC Code</TableHead>
            <TableHead>Balance</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {accounts.length === 0 ? (
            <TableRow>
              <TableCell colSpan={7} className="text-center text-muted-foreground py-8">
                No accounts found
              </TableCell>
            </TableRow>
          ) : (
            accounts.map((account) => (
              <motion.tr
                key={account.accountId || account.accountNumber}
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                className="hover:bg-muted/50 transition-colors"
              >
                <TableCell className="font-mono text-sm font-medium">
                  {account.accountNumber}
                </TableCell>
                <TableCell>{account.nameOnAccount}</TableCell>
                <TableCell>{account.bankName}</TableCell>
                <TableCell className="font-mono text-sm">{account.ifscCode}</TableCell>
                <TableCell className="font-semibold">{formatCurrency(account.amount)}</TableCell>
                <TableCell>
                  <span className={`px-2 py-1 rounded-full text-xs ${
                    account.status.toLowerCase() === 'active' 
                      ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400' 
                      : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
                  }`}>
                    {account.status}
                  </span>
                </TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => onView(account)}
                      title="View Details"
                    >
                      <Eye className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => onEdit(account)}
                      title="Edit Account"
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => onDelete(account)}
                      title="Delete Account"
                    >
                      <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                  </div>
                </TableCell>
              </motion.tr>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  );
};
