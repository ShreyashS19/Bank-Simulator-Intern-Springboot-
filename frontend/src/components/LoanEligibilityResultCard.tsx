import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { LoanEligibilityResultDto, API_BASE_URL } from '@/services/loanService';
import axios from '@/utils/axiosConfig';
import {
  AlertTriangle,
  CheckCircle2,
  ClipboardCheck,
  Copy,
  Download,
  Lightbulb,
  Mail,
  Printer,
  RotateCcw,
} from 'lucide-react';
import { toast } from 'sonner';

interface LoanEligibilityResultCardProps {
  result: LoanEligibilityResultDto;
  onApplyAgain: () => void;
}

const LoanEligibilityResultCard = ({ result, onApplyAgain }: LoanEligibilityResultCardProps) => {
  const isEligible = result.eligibilityStatus === 'ELIGIBLE';

  const handleDownloadPdf = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}${result.pdfDownloadPath}`, {
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `eligibility-${result.referenceNumber}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch {
      toast.error('Failed to download PDF. Please try again.');
    }
  };

  const handleCopyReference = async () => {
    try {
      await navigator.clipboard.writeText(result.referenceNumber);
      toast.success('Reference number copied');
    } catch {
      toast.error('Could not copy reference number');
    }
  };

  const formattedDate = new Date(result.generatedAt).toLocaleString();

  return (
    <>
      <style>
        {`@media print {
          .loan-actions {
            display: none !important;
          }
          .loan-letter-card {
            box-shadow: none !important;
            border: 1px solid #d4d4d8 !important;
          }
        }`}
      </style>

      <Card className="loan-letter-card bg-white rounded-2xl shadow-lg border border-slate-200">
        <CardHeader className="pb-4">
          <div className="flex items-center gap-4">
            <img src="/apple-touch-icon.png" alt="Bank Logo" className="w-16 h-16 object-contain" />
            <div>
              <CardTitle className="text-2xl font-serif tracking-wide text-slate-900">Bank Simulator</CardTitle>
              <p className="text-sm text-slate-600">Loan Eligibility Advisory Letter</p>
            </div>
          </div>
          <Separator className="mt-4" />
        </CardHeader>

        <CardContent className="space-y-6">
          <div
            className={`w-full rounded-xl border p-4 flex items-start gap-3 ${
              isEligible ? 'bg-green-50 border-green-200 text-green-800' : 'bg-red-50 border-red-200 text-red-800'
            }`}
          >
            {isEligible ? <CheckCircle2 className="w-5 h-5 mt-0.5" /> : <AlertTriangle className="w-5 h-5 mt-0.5" />}
            <div>
              <Badge className={isEligible ? 'bg-green-700 text-white' : 'bg-red-700 text-white'}>
                {result.eligibilityStatus.replace('_', ' ')}
              </Badge>
              <p className="mt-2 text-base font-semibold">
                {isEligible
                  ? `You appear ELIGIBLE for ₹${result.loanAmount.toLocaleString()} ${result.loanPurpose} loan`
                  : 'You may face difficulty for the requested loan amount'}
              </p>
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="rounded-lg border border-slate-200 p-4 bg-slate-50">
              <p className="text-xs uppercase tracking-wider text-slate-500">Reference Number</p>
              <div className="mt-2 flex items-center gap-2">
                <p className="text-lg font-semibold text-slate-900">{result.referenceNumber}</p>
                <Button variant="ghost" size="icon" onClick={handleCopyReference} className="h-8 w-8" title="Copy">
                  <Copy className="h-4 w-4" />
                </Button>
              </div>
            </div>
            <div className="rounded-lg border border-slate-200 p-4 bg-slate-50">
              <p className="text-xs uppercase tracking-wider text-slate-500">Assessment Date</p>
              <p className="mt-2 text-sm font-medium text-slate-900">{formattedDate}</p>
            </div>
            <div className="rounded-lg border border-slate-200 p-4 bg-slate-50">
              <p className="text-xs uppercase tracking-wider text-slate-500">Eligibility Score / 100</p>
              <p className="mt-2 text-lg font-semibold text-slate-900">{result.eligibilityScore}</p>
            </div>
            <div className="rounded-lg border border-slate-200 p-4 bg-slate-50">
              <p className="text-xs uppercase tracking-wider text-slate-500">Loan Amount</p>
              <p className="mt-2 text-lg font-semibold text-slate-900">₹{result.loanAmount.toLocaleString()}</p>
            </div>
          </div>

          <p className="text-slate-700 leading-7">{result.eligibilityMessage}</p>

          {result.improvementTips && result.improvementTips.length > 0 && (
            <div className="rounded-xl border border-blue-200 bg-blue-50 p-4">
              <h3 className="font-semibold text-blue-900 flex items-center gap-2">
                <Lightbulb className="h-5 w-5" /> AI Improvement Suggestions
              </h3>
              <ul className="mt-3 space-y-2 text-sm text-blue-800">
                {result.improvementTips.map((tip, index) => (
                  <li key={index} className="flex gap-2">
                    <span className="font-bold">•</span>
                    <span>{tip}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          <div>
            <h3 className="font-semibold text-slate-900 tracking-wide">Documents to carry to bank</h3>
            <ol className="mt-3 space-y-2">
              {result.requiredDocuments.map((document, index) => (
                <li key={document} className="flex gap-2 text-sm text-slate-700">
                  <span className="font-semibold text-slate-900 w-6">{index + 1}.</span>
                  <ClipboardCheck className="h-4 w-4 mt-0.5 text-slate-500" />
                  <span>{document}</span>
                </li>
              ))}
            </ol>
          </div>

          <div className="rounded-xl border border-amber-200 bg-amber-50 p-4">
            <h3 className="font-semibold text-amber-900">Important Notes</h3>
            <ul className="mt-2 space-y-1 text-sm text-amber-900">
              {result.specialNotes?.map((note) => (
                <li key={note}>- {note}</li>
              ))}
              {!result.specialNotes?.length && (
                <>
                  <li>- This is a PRELIMINARY check only.</li>
                  <li>- Final approval at bank discretion.</li>
                  <li>- Valid 30 days from issue.</li>
                  <li>- Quote reference number at branch.</li>
                </>
              )}
            </ul>
          </div>

          <div className="rounded-lg border border-slate-200 bg-slate-50 p-3 flex items-center gap-2 text-sm text-slate-700">
            <Mail className="h-4 w-4" />
            <span>Email sent to {result.customerEmail}</span>
          </div>

          <div className="loan-actions flex flex-wrap gap-3 pt-2">
            <Button onClick={handleDownloadPdf} className="gap-2">
              <Download className="h-4 w-4" />
              Download PDF Letter
            </Button>
            <Button variant="outline" onClick={() => window.print()} className="gap-2">
              <Printer className="h-4 w-4" />
              Print This Page
            </Button>
            <Button variant="secondary" onClick={onApplyAgain} className="gap-2">
              <RotateCcw className="h-4 w-4" />
              Apply for Another Loan
            </Button>
          </div>
        </CardContent>
      </Card>
    </>
  );
};

export default LoanEligibilityResultCard;
