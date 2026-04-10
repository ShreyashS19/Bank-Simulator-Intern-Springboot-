import { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { loanService, LoanApplicationRequest, LoanResponse } from '@/services/loanService';
import { Loader2, CheckCircle2, XCircle, AlertCircle, ChevronLeft, ChevronRight } from 'lucide-react';

interface FormData extends LoanApplicationRequest {}

interface FormErrors {
  [key: string]: string;
}

export const LoanApplicationForm = () => {
  const [currentStep, setCurrentStep] = useState(1);
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState<LoanResponse | null>(null);
  const [error, setError] = useState<string>('');
  
  const [formData, setFormData] = useState<FormData>({
    // Personal Information
    age: 0,
    employmentType: '',
    residenceYears: 0,
    hasGuarantor: false,
    
    // Financial Information
    monthlyIncome: 0,
    existingEmi: 0,
    creditScore: 0,
    existingLoans: 0,
    hasCollateral: false,
    repaymentHistory: '',
    
    // Loan Details
    loanAmount: 0,
    loanPurpose: '',
    loanTenure: 0,
  });

  const [errors, setErrors] = useState<FormErrors>({});

  const validateStep = (step: number): boolean => {
    const newErrors: FormErrors = {};

    if (step === 1) {
      // Personal Information validation
      if (!formData.age || formData.age < 18 || formData.age > 70) {
        newErrors.age = 'Age must be between 18 and 70';
      }
      if (!formData.employmentType) {
        newErrors.employmentType = 'Employment type is required';
      }
      if (formData.residenceYears < 0) {
        newErrors.residenceYears = 'Residence years cannot be negative';
      }
    }

    if (step === 2) {
      // Financial Information validation
      if (!formData.monthlyIncome || formData.monthlyIncome <= 0) {
        newErrors.monthlyIncome = 'Monthly income must be greater than 0';
      }
      if (formData.existingEmi < 0) {
        newErrors.existingEmi = 'Existing EMI cannot be negative';
      }
      if (!formData.creditScore || formData.creditScore < 300 || formData.creditScore > 900) {
        newErrors.creditScore = 'Credit score must be between 300 and 900';
      }
      if (formData.existingLoans < 0) {
        newErrors.existingLoans = 'Existing loans cannot be negative';
      }
      if (!formData.repaymentHistory) {
        newErrors.repaymentHistory = 'Repayment history is required';
      }
    }

    if (step === 3) {
      // Loan Details validation
      if (!formData.loanAmount || formData.loanAmount < 10000 || formData.loanAmount > 10000000) {
        newErrors.loanAmount = 'Loan amount must be between 10,000 and 10,000,000';
      }
      if (!formData.loanPurpose) {
        newErrors.loanPurpose = 'Loan purpose is required';
      }
      if (!formData.loanTenure || formData.loanTenure < 6 || formData.loanTenure > 360) {
        newErrors.loanTenure = 'Loan tenure must be between 6 and 360 months';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleNext = () => {
    if (validateStep(currentStep)) {
      setCurrentStep(currentStep + 1);
    }
  };

  const handlePrevious = () => {
    setCurrentStep(currentStep - 1);
    setErrors({});
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateStep(3)) {
      return;
    }

    setIsLoading(true);
    setError('');
    setResult(null);

    try {
      const response = await loanService.applyForLoan(formData);
      setResult(response);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to submit loan application. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const resetForm = () => {
    setCurrentStep(1);
    setFormData({
      age: 0,
      employmentType: '',
      residenceYears: 0,
      hasGuarantor: false,
      monthlyIncome: 0,
      existingEmi: 0,
      creditScore: 0,
      existingLoans: 0,
      hasCollateral: false,
      repaymentHistory: '',
      loanAmount: 0,
      loanPurpose: '',
      loanTenure: 0,
    });
    setErrors({});
    setResult(null);
    setError('');
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'APPROVED':
        return <CheckCircle2 className="h-6 w-6 text-green-500" />;
      case 'REJECTED':
        return <XCircle className="h-6 w-6 text-red-500" />;
      case 'UNDER_REVIEW':
        return <AlertCircle className="h-6 w-6 text-yellow-500" />;
      default:
        return <AlertCircle className="h-6 w-6 text-blue-500" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'APPROVED':
        return 'text-green-600 bg-green-50 border-green-200';
      case 'REJECTED':
        return 'text-red-600 bg-red-50 border-red-200';
      case 'UNDER_REVIEW':
        return 'text-yellow-600 bg-yellow-50 border-yellow-200';
      default:
        return 'text-blue-600 bg-blue-50 border-blue-200';
    }
  };

  if (result) {
    return (
      <div className="max-w-4xl mx-auto p-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              {getStatusIcon(result.status)}
              Loan Application Result
            </CardTitle>
            <CardDescription>Your loan application has been processed</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            {/* Status Badge */}
            <div className={`p-4 rounded-lg border ${getStatusColor(result.status)}`}>
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-semibold text-lg">Status: {result.status.replace('_', ' ')}</p>
                  <p className="text-sm">Loan ID: {result.loanId}</p>
                </div>
                <div className="text-right">
                  <p className="text-2xl font-bold">{result.eligibilityScore.toFixed(2)}%</p>
                  <p className="text-sm">Eligibility Score</p>
                </div>
              </div>
            </div>

            {/* Loan Details */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Loan Amount</Label>
                <p className="text-lg font-semibold">₹{result.loanAmount.toLocaleString()}</p>
              </div>
              
              {result.status !== 'REJECTED' && (
                <>
                  <div className="space-y-2">
                    <Label>Interest Rate</Label>
                    <p className="text-lg font-semibold">{result.interestRate}% per annum</p>
                  </div>
                  
                  <div className="space-y-2">
                    <Label>Monthly EMI</Label>
                    <p className="text-lg font-semibold">₹{result.emi.toLocaleString()}</p>
                  </div>
                  
                  <div className="space-y-2">
                    <Label>Loan Tenure</Label>
                    <p className="text-lg font-semibold">{result.loanTenure} months</p>
                  </div>
                </>
              )}
              
              <div className="space-y-2">
                <Label>DTI Ratio</Label>
                <p className="text-lg font-semibold">{(result.dtiRatio * 100).toFixed(2)}%</p>
              </div>
            </div>

            {/* Rejection Reason */}
            {result.rejectionReason && (
              <Alert variant="destructive">
                <AlertTitle>Rejection Reason</AlertTitle>
                <AlertDescription>{result.rejectionReason}</AlertDescription>
              </Alert>
            )}

            {/* Improvement Tips */}
            {result.improvementTips && result.improvementTips.length > 0 && (
              <Alert>
                <AlertTitle>Improvement Tips</AlertTitle>
                <AlertDescription>
                  <ul className="list-disc list-inside space-y-1 mt-2">
                    {result.improvementTips.map((tip, index) => (
                      <li key={index}>{tip}</li>
                    ))}
                  </ul>
                </AlertDescription>
              </Alert>
            )}

            <div className="flex justify-center">
              <Button onClick={resetForm}>Apply for Another Loan</Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto p-6">
      <Card>
        <CardHeader>
          <CardTitle>Loan Application</CardTitle>
          <CardDescription>Complete the form to apply for a loan</CardDescription>
          
          {/* Progress Indicator */}
          <div className="flex items-center justify-between mt-4">
            <div className="flex items-center gap-2">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                currentStep >= 1 ? 'bg-primary text-primary-foreground' : 'bg-gray-200'
              }`}>
                1
              </div>
              <div className={`h-1 w-16 ${currentStep >= 2 ? 'bg-primary' : 'bg-gray-200'}`} />
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                currentStep >= 2 ? 'bg-primary text-primary-foreground' : 'bg-gray-200'
              }`}>
                2
              </div>
              <div className={`h-1 w-16 ${currentStep >= 3 ? 'bg-primary' : 'bg-gray-200'}`} />
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                currentStep >= 3 ? 'bg-primary text-primary-foreground' : 'bg-gray-200'
              }`}>
                3
              </div>
            </div>
            <div className="text-sm text-muted-foreground">
              Step {currentStep} of 3
            </div>
          </div>
        </CardHeader>

        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Step 1: Personal Information */}
            {currentStep === 1 && (
              <div className="space-y-4">
                <h3 className="text-lg font-semibold">Personal Information</h3>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {/* Age */}
                  <div className="space-y-2">
                    <Label htmlFor="age">Age *</Label>
                    <Input
                      id="age"
                      type="number"
                      value={formData.age || ''}
                      onChange={(e) => setFormData({ ...formData, age: parseInt(e.target.value) || 0 })}
                      placeholder="Enter your age"
                    />
                    {errors.age && <p className="text-sm text-red-500">{errors.age}</p>}
                  </div>

                  {/* Employment Type */}
                  <div className="space-y-2">
                    <Label htmlFor="employmentType">Employment Type *</Label>
                    <Select
                      value={formData.employmentType}
                      onValueChange={(value) => setFormData({ ...formData, employmentType: value })}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select employment type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="SALARIED">Salaried</SelectItem>
                        <SelectItem value="SELF_EMPLOYED">Self Employed</SelectItem>
                        <SelectItem value="GOVERNMENT">Government</SelectItem>
                        <SelectItem value="UNEMPLOYED">Unemployed</SelectItem>
                      </SelectContent>
                    </Select>
                    {errors.employmentType && <p className="text-sm text-red-500">{errors.employmentType}</p>}
                  </div>

                  {/* Residence Years */}
                  <div className="space-y-2">
                    <Label htmlFor="residenceYears">Years at Current Residence *</Label>
                    <Input
                      id="residenceYears"
                      type="number"
                      value={formData.residenceYears || ''}
                      onChange={(e) => setFormData({ ...formData, residenceYears: parseInt(e.target.value) || 0 })}
                      placeholder="Enter years"
                    />
                    {errors.residenceYears && <p className="text-sm text-red-500">{errors.residenceYears}</p>}
                  </div>

                  {/* Has Guarantor */}
                  <div className="space-y-2 flex items-center gap-2 pt-8">
                    <Checkbox
                      id="hasGuarantor"
                      checked={formData.hasGuarantor}
                      onCheckedChange={(checked) => setFormData({ ...formData, hasGuarantor: checked as boolean })}
                    />
                    <Label htmlFor="hasGuarantor" className="cursor-pointer">I have a guarantor</Label>
                  </div>
                </div>
              </div>
            )}

            {/* Step 2: Financial Information */}
            {currentStep === 2 && (
              <div className="space-y-4">
                <h3 className="text-lg font-semibold">Financial Information</h3>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {/* Monthly Income */}
                  <div className="space-y-2">
                    <Label htmlFor="monthlyIncome">Monthly Income (₹) *</Label>
                    <Input
                      id="monthlyIncome"
                      type="number"
                      value={formData.monthlyIncome || ''}
                      onChange={(e) => setFormData({ ...formData, monthlyIncome: parseFloat(e.target.value) || 0 })}
                      placeholder="Enter monthly income"
                    />
                    {errors.monthlyIncome && <p className="text-sm text-red-500">{errors.monthlyIncome}</p>}
                  </div>

                  {/* Existing EMI */}
                  <div className="space-y-2">
                    <Label htmlFor="existingEmi">Existing Monthly EMI (₹) *</Label>
                    <Input
                      id="existingEmi"
                      type="number"
                      value={formData.existingEmi || ''}
                      onChange={(e) => setFormData({ ...formData, existingEmi: parseFloat(e.target.value) || 0 })}
                      placeholder="Enter existing EMI"
                    />
                    {errors.existingEmi && <p className="text-sm text-red-500">{errors.existingEmi}</p>}
                  </div>

                  {/* Credit Score */}
                  <div className="space-y-2">
                    <Label htmlFor="creditScore">Credit Score *</Label>
                    <Input
                      id="creditScore"
                      type="number"
                      value={formData.creditScore || ''}
                      onChange={(e) => setFormData({ ...formData, creditScore: parseInt(e.target.value) || 0 })}
                      placeholder="300-900"
                    />
                    {errors.creditScore && <p className="text-sm text-red-500">{errors.creditScore}</p>}
                  </div>

                  {/* Existing Loans */}
                  <div className="space-y-2">
                    <Label htmlFor="existingLoans">Number of Existing Loans *</Label>
                    <Input
                      id="existingLoans"
                      type="number"
                      value={formData.existingLoans || ''}
                      onChange={(e) => setFormData({ ...formData, existingLoans: parseInt(e.target.value) || 0 })}
                      placeholder="Enter number"
                    />
                    {errors.existingLoans && <p className="text-sm text-red-500">{errors.existingLoans}</p>}
                  </div>

                  {/* Repayment History */}
                  <div className="space-y-2">
                    <Label htmlFor="repaymentHistory">Repayment History *</Label>
                    <Select
                      value={formData.repaymentHistory}
                      onValueChange={(value) => setFormData({ ...formData, repaymentHistory: value })}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select repayment history" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="CLEAN">Clean</SelectItem>
                        <SelectItem value="NOT_CLEAN">Not Clean</SelectItem>
                      </SelectContent>
                    </Select>
                    {errors.repaymentHistory && <p className="text-sm text-red-500">{errors.repaymentHistory}</p>}
                  </div>

                  {/* Has Collateral */}
                  <div className="space-y-2 flex items-center gap-2 pt-8">
                    <Checkbox
                      id="hasCollateral"
                      checked={formData.hasCollateral}
                      onCheckedChange={(checked) => setFormData({ ...formData, hasCollateral: checked as boolean })}
                    />
                    <Label htmlFor="hasCollateral" className="cursor-pointer">I have collateral</Label>
                  </div>
                </div>
              </div>
            )}

            {/* Step 3: Loan Details */}
            {currentStep === 3 && (
              <div className="space-y-4">
                <h3 className="text-lg font-semibold">Loan Details</h3>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {/* Loan Amount */}
                  <div className="space-y-2">
                    <Label htmlFor="loanAmount">Loan Amount (₹) *</Label>
                    <Input
                      id="loanAmount"
                      type="number"
                      value={formData.loanAmount || ''}
                      onChange={(e) => setFormData({ ...formData, loanAmount: parseFloat(e.target.value) || 0 })}
                      placeholder="10,000 - 10,000,000"
                    />
                    {errors.loanAmount && <p className="text-sm text-red-500">{errors.loanAmount}</p>}
                  </div>

                  {/* Loan Purpose */}
                  <div className="space-y-2">
                    <Label htmlFor="loanPurpose">Loan Purpose *</Label>
                    <Select
                      value={formData.loanPurpose}
                      onValueChange={(value) => setFormData({ ...formData, loanPurpose: value })}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select loan purpose" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="EDUCATION">Education</SelectItem>
                        <SelectItem value="HOME">Home</SelectItem>
                        <SelectItem value="BUSINESS">Business</SelectItem>
                        <SelectItem value="PERSONAL">Personal</SelectItem>
                        <SelectItem value="VEHICLE">Vehicle</SelectItem>
                      </SelectContent>
                    </Select>
                    {errors.loanPurpose && <p className="text-sm text-red-500">{errors.loanPurpose}</p>}
                  </div>

                  {/* Loan Tenure */}
                  <div className="space-y-2">
                    <Label htmlFor="loanTenure">Loan Tenure (months) *</Label>
                    <Input
                      id="loanTenure"
                      type="number"
                      value={formData.loanTenure || ''}
                      onChange={(e) => setFormData({ ...formData, loanTenure: parseInt(e.target.value) || 0 })}
                      placeholder="6 - 360 months"
                    />
                    {errors.loanTenure && <p className="text-sm text-red-500">{errors.loanTenure}</p>}
                  </div>
                </div>
              </div>
            )}

            {/* Error Message */}
            {error && (
              <Alert variant="destructive">
                <AlertTitle>Error</AlertTitle>
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}

            {/* Navigation Buttons */}
            <div className="flex justify-between pt-4">
              <Button
                type="button"
                variant="outline"
                onClick={handlePrevious}
                disabled={currentStep === 1 || isLoading}
              >
                <ChevronLeft className="h-4 w-4 mr-2" />
                Previous
              </Button>

              {currentStep < 3 ? (
                <Button type="button" onClick={handleNext}>
                  Next
                  <ChevronRight className="h-4 w-4 ml-2" />
                </Button>
              ) : (
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin mr-2" />
                      Submitting...
                    </>
                  ) : (
                    'Submit Application'
                  )}
                </Button>
              )}
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};
