// import { useState, useEffect } from 'react';
// import { motion, AnimatePresence } from 'framer-motion';
// import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
// import { Button } from '@/components/ui/button';
// import { Input } from '@/components/ui/input';
// import { Label } from '@/components/ui/label';
// import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
// import { Checkbox } from '@/components/ui/checkbox';
// import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
// import { loanService, LoanApplicationRequest, LoanResponse } from '@/services/loanService';
// import { Loader2, CheckCircle2, XCircle, AlertCircle, ChevronLeft, ChevronRight, DollarSign, Wallet, Briefcase, Activity, Target } from 'lucide-react';
// import { DotLottieReact } from '@lottiefiles/dotlottie-react';

// type FormData = LoanApplicationRequest;
// interface FormErrors { [key: string]: string; }

// const slideVariants = {
//   hidden: { opacity: 0, x: 20 },
//   visible: { opacity: 1, x: 0, transition: { duration: 0.4, ease: 'easeOut' } },
//   exit: { opacity: 0, x: -20, transition: { duration: 0.3 } }
// };

// export const LoanApplicationForm = () => {
//   const [currentStep, setCurrentStep] = useState(1);
//   const [isLoading, setIsLoading] = useState(false);
//   const [result, setResult] = useState<LoanResponse | null>(null);
//   const [error, setError] = useState<string>('');
  
//   const [formData, setFormData] = useState<FormData>({
//     age: 0, employmentType: '', residenceYears: 0, hasGuarantor: false,
//     monthlyIncome: 0, existingEmi: 0, creditScore: 0, existingLoans: 0, hasCollateral: false, repaymentHistory: '',
//     loanAmount: 0, loanPurpose: '', loanTenure: 0,
//   });

//   const [errors, setErrors] = useState<FormErrors>({});

//   const validateStep = (step: number): boolean => {
//     const newErrors: FormErrors = {};
//     if (step === 1) {
//       if (!formData.age || formData.age < 18 || formData.age > 70) newErrors.age = 'Age must be between 18 and 70';
//       if (!formData.employmentType) newErrors.employmentType = 'Required';
//       if (formData.residenceYears < 0) newErrors.residenceYears = 'Invalid years';
//     }
//     if (step === 2) {
//       if (!formData.monthlyIncome || formData.monthlyIncome <= 0) newErrors.monthlyIncome = 'Must be > 0';
//       if (formData.existingEmi < 0) newErrors.existingEmi = 'Invalid EMI';
//       if (!formData.creditScore || formData.creditScore < 300 || formData.creditScore > 900) newErrors.creditScore = '300-900 only';
//       if (formData.existingLoans < 0) newErrors.existingLoans = 'Invalid loans';
//       if (!formData.repaymentHistory) newErrors.repaymentHistory = 'Required';
//     }
//     if (step === 3) {
//       if (!formData.loanAmount || formData.loanAmount < 10000 || formData.loanAmount > 10000000) newErrors.loanAmount = '₹10k - ₹1Cr';
//       if (!formData.loanPurpose) newErrors.loanPurpose = 'Required';
//       if (!formData.loanTenure || formData.loanTenure < 6 || formData.loanTenure > 360) newErrors.loanTenure = '6-360 months';
//     }
//     setErrors(newErrors);
//     return Object.keys(newErrors).length === 0;
//   };

//   const handleNext = () => { if (validateStep(currentStep)) setCurrentStep(currentStep + 1); };
//   const handlePrevious = () => { setCurrentStep(currentStep - 1); setErrors({}); };

//   const handleSubmit = async (e: React.FormEvent) => {
//     e.preventDefault();
//     if (!validateStep(3)) return;
//     setIsLoading(true); setError(''); setResult(null);
//     try {
//       const response = await loanService.applyForLoan(formData);
//       setResult(response);
//     } catch (err: any) {
//       setError(err.response?.data?.message || 'Failed to submit application.');
//     } finally {
//       setIsLoading(false);
//     }
//   };

//   const resetForm = () => {
//     setCurrentStep(1); setResult(null); setError(''); setErrors({});
//     setFormData({ age: 0, employmentType: '', residenceYears: 0, hasGuarantor: false, monthlyIncome: 0, existingEmi: 0, creditScore: 0, existingLoans: 0, hasCollateral: false, repaymentHistory: '', loanAmount: 0, loanPurpose: '', loanTenure: 0 });
//   };

//   if (result) {
//     return (
//       <motion.div initial={{ opacity: 0, scale: 0.9 }} animate={{ opacity: 1, scale: 1 }} className="max-w-2xl mx-auto p-4 md:p-8">
//         <Card className="glass-card overflow-hidden border-0 relative">
//           <div className="absolute inset-0 bg-gradient-to-br from-primary/10 to-transparent"></div>
//           <CardContent className="p-8 relative z-10 text-center">
            
//             {result.status === 'APPROVED' ? (
//               <div className="w-48 h-48 mx-auto -mt-6">
//                 <DotLottieReact
//                   src="https://lottie.host/79075727-8d07-477d-ba32-474bd634e9e0/A3lS6XU3T3.lottie"
//                   loop={false}
//                   autoplay
//                 />
//               </div>
//             ) : (
//               <div className="mx-auto w-20 h-20 bg-amber-500/10 rounded-full flex items-center justify-center mb-6">
//                 <AlertCircle className="w-10 h-10 text-amber-500" />
//               </div>
//             )}
            
//             <h2 className={`text-4xl font-extrabold tracking-tight mt-2 ${result.status === 'APPROVED' ? 'text-emerald-500' : result.status === 'REJECTED' ? 'text-red-500' : 'text-amber-500'}`}>
//               {result.status.replace('_', ' ')}
//             </h2>
//             <p className="text-muted-foreground mt-2 text-lg">Application ID: {result.loanId}</p>

//             <div className="mt-8 grid grid-cols-2 gap-4 text-left">
//               <div className="p-4 rounded-2xl bg-white/5 border border-white/10 glass-panel">
//                 <p className="text-muted-foreground text-sm font-medium">Eligibility Score</p>
//                 <p className="text-2xl font-bold text-foreground mt-1">{result.eligibilityScore.toFixed(0)}%</p>
//               </div>
//               <div className="p-4 rounded-2xl bg-white/5 border border-white/10 glass-panel">
//                 <p className="text-muted-foreground text-sm font-medium">Amount</p>
//                 <p className="text-2xl font-bold text-foreground mt-1">₹{result.loanAmount.toLocaleString()}</p>
//               </div>
//               {result.status !== 'REJECTED' && (
//                 <>
//                   <div className="p-4 rounded-2xl bg-white/5 border border-white/10 glass-panel">
//                     <p className="text-muted-foreground text-sm font-medium">EMI Estimate</p>
//                     <p className="text-2xl font-bold text-primary mt-1">₹{result.emi.toLocaleString()}</p>
//                   </div>
//                   <div className="p-4 rounded-2xl bg-white/5 border border-white/10 glass-panel">
//                     <p className="text-muted-foreground text-sm font-medium">Interest Rate</p>
//                     <p className="text-2xl font-bold text-foreground mt-1">{result.interestRate}%</p>
//                   </div>
//                 </>
//               )}
//             </div>

//             {result.improvementTips && result.improvementTips.length > 0 && (
//               <div className="mt-8 text-left p-6 rounded-2xl bg-black/5 dark:bg-white/5 border border-white/10">
//                 <h4 className="font-semibold text-foreground flex items-center gap-2">
//                   <Activity className="w-5 h-5 text-primary"/> AI Review Tips
//                 </h4>
//                 <ul className="mt-3 space-y-2">
//                   {result.improvementTips.map((tip, i) => (
//                     <li key={i} className="text-sm border-l-2 border-primary/40 pl-3 text-muted-foreground">{tip}</li>
//                   ))}
//                 </ul>
//               </div>
//             )}

//             <Button onClick={resetForm} className="mt-8 w-full md:w-auto px-8 rounded-full h-12 text-md shadow-lg shadow-primary/20 transition-all hover:scale-105">
//               Back to Dashboard
//             </Button>
//           </CardContent>
//         </Card>
//       </motion.div>
//     );
//   }

//   const RenderStepIndicator = () => (
//     <div className="mb-10 mt-2">
//       <div className="flex items-center justify-between relative max-w-sm mx-auto">
//         <div className="absolute left-0 right-0 top-1/2 h-1 bg-border/50 -z-10 rounded-full" />
//         <motion.div 
//             className="absolute left-0 top-1/2 h-1 bg-primary -z-10 rounded-full origin-left"
//             initial={{ width: '0%' }}
//             animate={{ width: `${((currentStep - 1) / 2) * 100}%` }}
//             transition={{ duration: 0.5, ease: 'easeInOut' }}
//         />
        
//         {[1, 2, 3].map((step) => {
//           const isActive = step === currentStep;
//           const isCompleted = step < currentStep;
//           return (
//             <div key={step} className="flex flex-col items-center">
//               <motion.div 
//                 animate={{ 
//                   scale: isActive ? 1.2 : 1,
//                   backgroundColor: isActive || isCompleted ? 'hsl(var(--primary))' : 'hsl(var(--muted))'
//                 }}
//                 className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold shadow-lg transition-colors ${isActive || isCompleted ? 'text-primary-foreground' : 'text-muted-foreground'}`}
//               >
//                 {isCompleted ? <CheckCircle2 className="w-5 h-5" /> : step}
//               </motion.div>
//               <div className="absolute top-12 text-xs font-medium text-muted-foreground whitespace-nowrap">
//                 {step === 1 ? 'Personal' : step === 2 ? 'Financial' : 'Loan Details'}
//               </div>
//             </div>
//           );
//         })}
//       </div>
//     </div>
//   );

//   return (
//     <div className="max-w-3xl mx-auto p-4 md:p-8">
//       <Card className="glass-card shadow-2xl border-white/20 relative overflow-hidden">
//         {/* Decorative Gradients */}
//         <div className="absolute top-0 right-0 w-[400px] h-[400px] bg-primary/10 rounded-full blur-[80px] -mr-40 -mt-40 z-0 pointer-events-none" />
//         <div className="absolute bottom-0 left-0 w-[300px] h-[300px] bg-accent/10 rounded-full blur-[80px] -ml-20 -mb-20 z-0 pointer-events-none" />
        
//         <CardHeader className="relative z-10 text-center pb-2">
//           <CardTitle className="text-3xl font-extrabold bg-clip-text text-transparent bg-gradient-to-r from-primary to-primary/60">
//             Apply for Loan
//           </CardTitle>
//           <CardDescription className="text-md">AI-powered instant pre-approval</CardDescription>
//         </CardHeader>

//         <CardContent className="relative z-10 pt-4">
//           <RenderStepIndicator />

//           <form onSubmit={handleSubmit} className="mt-12 space-y-8">
//             <AnimatePresence mode="wait">
//               {currentStep === 1 && (
//                 <motion.div key="step1" variants={slideVariants} initial="hidden" animate="visible" exit="exit" className="space-y-6">
//                   <div className="flex items-center gap-2 mb-6 text-xl font-semibold border-b border-border/50 pb-2">
//                     <Briefcase className="w-5 h-5 text-primary" /> Personal Profile
//                   </div>
                  
//                   <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
//                     <InputField id="age" label="Age" type="number" value={formData.age} onChange={(v) => setFormData({...formData, age: v})} error={errors.age} placeholder="e.g. 28" />
                    
//                     <div className="space-y-2">
//                       <Label className="text-muted-foreground font-medium">Employment Type</Label>
//                       <Select value={formData.employmentType} onValueChange={(v) => setFormData({...formData, employmentType: v})}>
//                         <SelectTrigger className="h-12 bg-white/5 border-white/10 focus:ring-primary/40 rounded-xl transition-all">
//                           <SelectValue placeholder="Select type" />
//                         </SelectTrigger>
//                         <SelectContent className="rounded-xl border-white/10 glass-panel">
//                           <SelectItem value="SALARIED">Salaried</SelectItem>
//                           <SelectItem value="SELF_EMPLOYED">Self Employed</SelectItem>
//                           <SelectItem value="GOVERNMENT">Government</SelectItem>
//                         </SelectContent>
//                       </Select>
//                       {errors.employmentType && <p className="text-xs text-red-500 pl-1">{errors.employmentType}</p>}
//                     </div>

//                     <InputField id="residenceYears" label="Years at current residence" type="number" value={formData.residenceYears} onChange={(v) => setFormData({...formData, residenceYears: v})} error={errors.residenceYears} placeholder="Years" />
                    
//                     <div className="flex items-center h-full pt-6">
//                       <div className="flex items-center space-x-3 bg-white/5 p-3 rounded-xl border border-white/10 w-full cursor-pointer hover:bg-white/10 transition">
//                         <Checkbox id="hasGuarantor" checked={formData.hasGuarantor} onCheckedChange={(c) => setFormData({...formData, hasGuarantor: c as boolean})} className="data-[state=checked]:bg-primary h-5 w-5 border-primary/30" />
//                         <Label htmlFor="hasGuarantor" className="cursor-pointer flex-1 font-medium text-foreground">I have a guarantor</Label>
//                       </div>
//                     </div>
//                   </div>
//                 </motion.div>
//               )}

//               {currentStep === 2 && (
//                 <motion.div key="step2" variants={slideVariants} initial="hidden" animate="visible" exit="exit" className="space-y-6">
//                   <div className="flex items-center gap-2 mb-6 text-xl font-semibold border-b border-border/50 pb-2">
//                     <Wallet className="w-5 h-5 text-primary" /> Financial Data
//                   </div>
                  
//                   <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
//                     <InputField id="monthlyIncome" label="Monthly Income (₹)" type="number" value={formData.monthlyIncome} onChange={(v) => setFormData({...formData, monthlyIncome: v})} error={errors.monthlyIncome} placeholder="e.g. 85000" />
//                     <InputField id="existingEmi" label="Existing EMI (₹)" type="number" value={formData.existingEmi} onChange={(v) => setFormData({...formData, existingEmi: v})} error={errors.existingEmi} placeholder="e.g. 15000" />
//                     <InputField id="creditScore" label="Credit Score" type="number" value={formData.creditScore} onChange={(v) => setFormData({...formData, creditScore: v})} error={errors.creditScore} placeholder="300-900" />
//                     <InputField id="existingLoans" label="Active Loans count" type="number" value={formData.existingLoans} onChange={(v) => setFormData({...formData, existingLoans: v})} error={errors.existingLoans} placeholder="e.g. 1" />
                    
//                     <div className="space-y-2">
//                       <Label className="text-muted-foreground font-medium">Repayment History</Label>
//                       <Select value={formData.repaymentHistory} onValueChange={(v) => setFormData({...formData, repaymentHistory: v})}>
//                         <SelectTrigger className="h-12 bg-white/5 border-white/10 focus:ring-primary/40 rounded-xl transition-all">
//                           <SelectValue placeholder="Select history" />
//                         </SelectTrigger>
//                         <SelectContent className="rounded-xl border-white/10 glass-panel">
//                           <SelectItem value="CLEAN">Clean (No defaults)</SelectItem>
//                           <SelectItem value="NOT_CLEAN">Not Clean</SelectItem>
//                         </SelectContent>
//                       </Select>
//                       {errors.repaymentHistory && <p className="text-xs text-red-500 pl-1">{errors.repaymentHistory}</p>}
//                     </div>

//                     <div className="flex items-center h-full pt-6">
//                       <div className="flex items-center space-x-3 bg-white/5 p-3 rounded-xl border border-white/10 w-full cursor-pointer hover:bg-white/10 transition">
//                         <Checkbox id="hasCollateral" checked={formData.hasCollateral} onCheckedChange={(c) => setFormData({...formData, hasCollateral: c as boolean})} className="data-[state=checked]:bg-primary h-5 w-5 border-primary/30" />
//                         <Label htmlFor="hasCollateral" className="cursor-pointer flex-1 font-medium text-foreground">Provide Collateral / Asset</Label>
//                       </div>
//                     </div>
//                   </div>
//                 </motion.div>
//               )}

//               {currentStep === 3 && (
//                 <motion.div key="step3" variants={slideVariants} initial="hidden" animate="visible" exit="exit" className="space-y-6">
//                   <div className="flex items-center gap-2 mb-6 text-xl font-semibold border-b border-border/50 pb-2">
//                     <Target className="w-5 h-5 text-primary" /> Loan Config
//                   </div>
                  
//                   <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
//                     <InputField id="loanAmount" label="Loan Amount (₹)" type="number" value={formData.loanAmount} onChange={(v) => setFormData({...formData, loanAmount: v})} error={errors.loanAmount} placeholder="e.g. 500000" />
                    
//                     <div className="space-y-2">
//                       <Label className="text-muted-foreground font-medium">Loan Purpose</Label>
//                       <Select value={formData.loanPurpose} onValueChange={(v) => setFormData({...formData, loanPurpose: v})}>
//                         <SelectTrigger className="h-12 bg-white/5 border-white/10 focus:ring-primary/40 rounded-xl transition-all">
//                           <SelectValue placeholder="Select purpose" />
//                         </SelectTrigger>
//                         <SelectContent className="rounded-xl border-white/10 glass-panel">
//                           <SelectItem value="HOME">Home</SelectItem>
//                           <SelectItem value="VEHICLE">Vehicle (Auto)</SelectItem>
//                           <SelectItem value="PERSONAL">Personal Needs</SelectItem>
//                           <SelectItem value="BUSINESS">Business / Startup</SelectItem>
//                           <SelectItem value="EDUCATION">Education</SelectItem>
//                         </SelectContent>
//                       </Select>
//                       {errors.loanPurpose && <p className="text-xs text-red-500 pl-1">{errors.loanPurpose}</p>}
//                     </div>

//                     <InputField id="loanTenure" label="Tenure (Months)" type="number" value={formData.loanTenure} onChange={(v) => setFormData({...formData, loanTenure: v})} error={errors.loanTenure} placeholder="e.g. 48" />
                    
//                     <div className="p-4 rounded-xl bg-primary/5 flex items-start gap-3 border border-primary/10">
//                        <DollarSign className="w-6 h-6 text-primary shrink-0" />
//                        <div>
//                          <p className="text-sm font-semibold text-foreground">Smart AI Suggestion</p>
//                          <p className="text-xs text-muted-foreground mt-1">For ₹{formData.loanAmount || 0}, an optimal tenure is 48 months to keep EMI within 30% of your income.</p>
//                        </div>
//                     </div>
//                   </div>
//                 </motion.div>
//               )}
//             </AnimatePresence>

//             {error && (
//               <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}>
//                 <Alert variant="destructive" className="glass-card mt-6">
//                   <AlertCircle className="h-4 w-4" />
//                   <AlertTitle>Submission Error</AlertTitle>
//                   <AlertDescription>{error}</AlertDescription>
//                 </Alert>
//               </motion.div>
//             )}

//             <div className="flex justify-between mt-8 pt-6 border-t border-border/40">
//               <Button type="button" variant="outline" className="rounded-full px-6 h-12 bg-transparent hover:bg-white/5 border-white/20 transition-all font-medium" onClick={handlePrevious} disabled={currentStep === 1 || isLoading}>
//                 <ChevronLeft className="h-4 w-4 mr-2" /> Back
//               </Button>

//               {currentStep < 3 ? (
//                 <Button type="button" className="rounded-full px-8 h-12 shadow-md shadow-primary/20 hover:shadow-lg transition-all font-medium group" onClick={handleNext}>
//                   Continue <ChevronRight className="h-4 w-4 ml-1 group-hover:translate-x-1 transition-transform" />
//                 </Button>
//               ) : (
//                 <Button type="submit" disabled={isLoading} className="rounded-full px-10 h-12 shadow-lg shadow-primary/30 hover:scale-[1.02] transition-all font-medium group">
//                   {isLoading ? (
//                     <><Loader2 className="h-5 w-5 animate-spin mr-2" /> Processing...</>
//                   ) : (
//                      <>Submit Application <CheckCircle2 className="h-4 w-4 ml-2 group-hover:scale-110 transition-transform" /></>
//                   )}
//                 </Button>
//               )}
//             </div>
//           </form>
//         </CardContent>
//       </Card>
//     </div>
//   );
// };

// // Reusable animated input field component
// const InputField = ({ id, label, type, value, onChange, error, placeholder }: any) => (
//   <div className="space-y-2 group relative">
//     <Label htmlFor={id} className="text-muted-foreground font-medium group-focus-within:text-primary transition-colors">
//       {label}
//     </Label>
//     <Input
//       id={id}
//       type={type}
//       value={value || ''}
//       onChange={(e) => onChange(e.target.value === '' ? '' : parseFloat(e.target.value) || 0)}
//       placeholder={placeholder}
//       className="h-12 bg-white/5 border-white/10 hover:border-white/20 focus:border-primary focus:ring-primary/30 rounded-xl transition-all w-full text-foreground pl-4 shadow-sm group-focus-within:shadow-md"
//     />
//     {error && (
//       <motion.p initial={{ opacity: 0, y: -5 }} animate={{ opacity: 1, y: 0 }} className="text-xs text-red-500 pl-1 absolute -bottom-5">
//         {error}
//       </motion.p>
//     )}
//   </div>
// );
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence, Variants } from 'framer-motion';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { loanService, LoanApplicationRequest, LoanEligibilityResultDto } from '@/services/loanService';
import { Loader2, CheckCircle2, AlertCircle, ChevronLeft, ChevronRight, DollarSign, Wallet, Briefcase, Target, HelpCircle } from 'lucide-react';
import LoanEligibilityResultCard from '@/components/LoanEligibilityResultCard';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';

type FormData = LoanApplicationRequest;
interface FormErrors { [key: string]: string; }

const slideVariants: Variants = {
  hidden: { opacity: 0, x: 20 },
  visible: { opacity: 1, x: 0, transition: { duration: 0.4, ease: 'easeOut' as const } },
  exit: { opacity: 0, x: -20, transition: { duration: 0.3 } }
};

export const LoanApplicationForm = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(1);
  const [isLoading, setIsLoading] = useState(false);
  const [isCreditScoreModalOpen, setIsCreditScoreModalOpen] = useState(false);
  const [eligibilityResult, setEligibilityResult] = useState<LoanEligibilityResultDto | null>(null);
  const [error, setError] = useState<string>('');
  
  const [formData, setFormData] = useState<FormData>({
    age: 0, employmentType: '', residenceYears: 0, hasGuarantor: false,
    monthlyIncome: 0, existingEmi: 0, creditScore: 0, existingLoans: 0, hasCollateral: false, repaymentHistory: '',
    loanAmount: 0, loanPurpose: '', loanTenure: 0,
  });

  const [errors, setErrors] = useState<FormErrors>({});

  const validateStep = (step: number): boolean => {
    const newErrors: FormErrors = {};
    if (step === 1) {
      if (!formData.age || formData.age < 18 || formData.age > 70) newErrors.age = 'Age must be between 18 and 70';
      if (!formData.employmentType) newErrors.employmentType = 'Required';
      if (formData.residenceYears < 0 || formData.residenceYears === ('' as any)) newErrors.residenceYears = 'Must be 0 or more';
    }
    if (step === 2) {
      if (!formData.monthlyIncome || formData.monthlyIncome <= 0) newErrors.monthlyIncome = 'Must be > 0';
      if (formData.existingEmi < 0 || formData.existingEmi === ('' as any)) newErrors.existingEmi = 'Must be 0 or more';
      if (!formData.creditScore || formData.creditScore < 300 || formData.creditScore > 900) newErrors.creditScore = '300-900 only';
      if (formData.existingLoans < 0 || formData.existingLoans === ('' as any)) newErrors.existingLoans = 'Must be 0 or more';
      if (!formData.repaymentHistory) newErrors.repaymentHistory = 'Required';
    }
    if (step === 3) {
      if (!formData.loanAmount || formData.loanAmount < 10000 || formData.loanAmount > 10000000) newErrors.loanAmount = '₹10k - ₹1Cr';
      if (!formData.loanPurpose) newErrors.loanPurpose = 'Required';
      if (!formData.loanTenure || formData.loanTenure < 6 || formData.loanTenure > 360) newErrors.loanTenure = '6-360 months';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleNext = () => { if (validateStep(currentStep)) setCurrentStep(currentStep + 1); };
  const handlePrevious = () => { 
    if (currentStep === 1) {
      navigate('/loans');
      return;
    }
    setCurrentStep(currentStep - 1); 
    setErrors({}); 
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateStep(3)) return;
    setIsLoading(true); setError(''); setEligibilityResult(null);
    try {
      const response = await loanService.applyForLoan(formData);
      setEligibilityResult(response);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to submit application.');
    } finally {
      setIsLoading(false);
    }
  };

  const resetForm = () => {
    setCurrentStep(1); setEligibilityResult(null); setError(''); setErrors({});
    setFormData({ age: 0, employmentType: '', residenceYears: 0, hasGuarantor: false, monthlyIncome: 0, existingEmi: 0, creditScore: 0, existingLoans: 0, hasCollateral: false, repaymentHistory: '', loanAmount: 0, loanPurpose: '', loanTenure: 0 });
  };

  const RenderStepIndicator = () => (
    <div className="mb-10 mt-2">
      <div className="flex items-center justify-between relative max-w-sm mx-auto">
        <div className="absolute left-0 right-0 top-1/2 h-1 bg-border/50 -z-10 rounded-full" />
        <motion.div 
            className="absolute left-0 top-1/2 h-1 bg-primary -z-10 rounded-full origin-left"
            initial={{ width: '0%' }}
            animate={{ width: `${((currentStep - 1) / 2) * 100}%` }}
            transition={{ duration: 0.5, ease: 'easeInOut' as const }}
        />
        
        {[1, 2, 3].map((step) => {
          const isActive = step === currentStep;
          const isCompleted = step < currentStep;
          return (
            <div key={step} className="flex flex-col items-center">
              <motion.div 
                animate={{ scale: isActive ? 1.2 : 1 }}
                transition={{ duration: 0.2 }}
                className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold shadow-lg transition-colors duration-300
                  ${isActive || isCompleted
                    ? 'bg-primary text-primary-foreground'
                    : 'bg-muted text-muted-foreground'
                  }`}
              >
                {isCompleted ? <CheckCircle2 className="w-5 h-5" /> : step}
              </motion.div>
              <div className="absolute top-12 text-xs font-medium text-muted-foreground whitespace-nowrap">
                {step === 1 ? 'Personal' : step === 2 ? 'Financial' : 'Loan Details'}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );

  if (eligibilityResult) {
    return (
      <div className="max-w-5xl mx-auto p-4 md:p-8">
        <LoanEligibilityResultCard result={eligibilityResult} onApplyAgain={resetForm} />
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto p-4 md:p-8">
      <Card className="glass-card shadow-2xl border-white/20 relative overflow-hidden">
        {/* Decorative Gradients */}
        <div className="absolute top-0 right-0 w-[400px] h-[400px] bg-primary/10 rounded-full blur-[80px] -mr-40 -mt-40 z-0 pointer-events-none" />
        <div className="absolute bottom-0 left-0 w-[300px] h-[300px] bg-accent/10 rounded-full blur-[80px] -ml-20 -mb-20 z-0 pointer-events-none" />
        
        <CardHeader className="relative z-10 text-center pb-2">
          <CardTitle className="text-3xl font-extrabold bg-clip-text text-transparent bg-gradient-to-r from-primary to-primary/60">
            Apply for Loan
          </CardTitle>
          <CardDescription className="text-md">AI-powered instant pre-approval</CardDescription>
        </CardHeader>

        <CardContent className="relative z-10 pt-4">
          <RenderStepIndicator />

          <form onSubmit={handleSubmit} className="mt-12 space-y-8">
            <AnimatePresence mode="wait">
              {currentStep === 1 && (
                <motion.div key="step1" variants={slideVariants} initial="hidden" animate="visible" exit="exit" className="space-y-6">
                  <div className="flex items-center gap-2 mb-6 text-xl font-semibold border-b border-border/50 pb-2">
                    <Briefcase className="w-5 h-5 text-primary" /> Personal Profile
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
                    <InputField id="age" label="Age" type="number" value={formData.age} onChange={(v: number) => setFormData({...formData, age: v})} error={errors.age} placeholder="e.g. 28" />
                    
                    <div className="space-y-2">
                      <Label className="text-muted-foreground font-medium">Employment Type</Label>
                      <Select value={formData.employmentType} onValueChange={(v) => setFormData({...formData, employmentType: v})}>
                        <SelectTrigger className="h-12 bg-white/5 border-white/10 focus:ring-primary/40 rounded-xl transition-all">
                          <SelectValue placeholder="Select type" />
                        </SelectTrigger>
                        <SelectContent className="rounded-xl border-white/10 glass-panel">
                          <SelectItem value="SALARIED">Salaried</SelectItem>
                          <SelectItem value="SELF_EMPLOYED">Self Employed</SelectItem>
                          <SelectItem value="GOVERNMENT">Government</SelectItem>
                        </SelectContent>
                      </Select>
                      {errors.employmentType && <p className="text-xs text-red-500 pl-1">{errors.employmentType}</p>}
                    </div>

                    <InputField id="residenceYears" label="Years at current residence" type="number" value={formData.residenceYears} onChange={(v: number) => setFormData({...formData, residenceYears: v})} error={errors.residenceYears} placeholder="Years" />
                    
                    <div className="flex items-center h-full pt-6">
                      <div className="flex items-center space-x-3 bg-white/5 p-3 rounded-xl border border-white/10 w-full cursor-pointer hover:bg-white/10 transition">
                        <Checkbox id="hasGuarantor" checked={formData.hasGuarantor} onCheckedChange={(c) => setFormData({...formData, hasGuarantor: c as boolean})} className="data-[state=checked]:bg-primary h-5 w-5 border-primary/30" />
                        <Label htmlFor="hasGuarantor" className="cursor-pointer flex-1 font-medium text-foreground">I have a guarantor</Label>
                      </div>
                    </div>
                  </div>
                </motion.div>
              )}

              {currentStep === 2 && (
                <motion.div key="step2" variants={slideVariants} initial="hidden" animate="visible" exit="exit" className="space-y-6">
                  <div className="flex items-center gap-2 mb-6 text-xl font-semibold border-b border-border/50 pb-2">
                    <Wallet className="w-5 h-5 text-primary" /> Financial Data
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
                    <InputField id="monthlyIncome" label="Monthly Income (₹)" type="number" value={formData.monthlyIncome} onChange={(v: number) => setFormData({...formData, monthlyIncome: v})} error={errors.monthlyIncome} placeholder="e.g. 85000" />
                    <InputField id="existingEmi" label="Existing EMI (₹)" type="number" value={formData.existingEmi} onChange={(v: number) => setFormData({...formData, existingEmi: v})} error={errors.existingEmi} placeholder="e.g. 15000" />
                    <InputField id="creditScore" label="Credit Score" type="number" value={formData.creditScore} onChange={(v: number) => setFormData({...formData, creditScore: v})} error={errors.creditScore} placeholder="300-900" onHelpClick={() => setIsCreditScoreModalOpen(true)} />
                    <InputField id="existingLoans" label="Active Loans count" type="number" value={formData.existingLoans} onChange={(v: number) => setFormData({...formData, existingLoans: v})} error={errors.existingLoans} placeholder="e.g. 1" />
                    
                    <div className="space-y-2">
                      <Label className="text-muted-foreground font-medium">Repayment History</Label>
                      <Select value={formData.repaymentHistory} onValueChange={(v) => setFormData({...formData, repaymentHistory: v})}>
                        <SelectTrigger className="h-12 bg-white/5 border-white/10 focus:ring-primary/40 rounded-xl transition-all">
                          <SelectValue placeholder="Select history" />
                        </SelectTrigger>
                        <SelectContent className="rounded-xl border-white/10 glass-panel">
                          <SelectItem value="CLEAN">Clean (No defaults)</SelectItem>
                          <SelectItem value="NOT_CLEAN">Not Clean</SelectItem>
                        </SelectContent>
                      </Select>
                      {errors.repaymentHistory && <p className="text-xs text-red-500 pl-1">{errors.repaymentHistory}</p>}
                    </div>

                    <div className="flex items-center h-full pt-6">
                      <div className="flex items-center space-x-3 bg-white/5 p-3 rounded-xl border border-white/10 w-full cursor-pointer hover:bg-white/10 transition">
                        <Checkbox id="hasCollateral" checked={formData.hasCollateral} onCheckedChange={(c) => setFormData({...formData, hasCollateral: c as boolean})} className="data-[state=checked]:bg-primary h-5 w-5 border-primary/30" />
                        <Label htmlFor="hasCollateral" className="cursor-pointer flex-1 font-medium text-foreground">Provide Collateral / Asset</Label>
                      </div>
                    </div>
                  </div>
                </motion.div>
              )}

              {currentStep === 3 && (
                <motion.div key="step3" variants={slideVariants} initial="hidden" animate="visible" exit="exit" className="space-y-6">
                  <div className="flex items-center gap-2 mb-6 text-xl font-semibold border-b border-border/50 pb-2">
                    <Target className="w-5 h-5 text-primary" /> Loan Config
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
                    <InputField id="loanAmount" label="Loan Amount (₹)" type="number" value={formData.loanAmount} onChange={(v: number) => setFormData({...formData, loanAmount: v})} error={errors.loanAmount} placeholder="e.g. 500000" />
                    
                    <div className="space-y-2">
                      <Label className="text-muted-foreground font-medium">Loan Purpose</Label>
                      <Select value={formData.loanPurpose} onValueChange={(v) => setFormData({...formData, loanPurpose: v})}>
                        <SelectTrigger className="h-12 bg-white/5 border-white/10 focus:ring-primary/40 rounded-xl transition-all">
                          <SelectValue placeholder="Select purpose" />
                        </SelectTrigger>
                        <SelectContent className="rounded-xl border-white/10 glass-panel">
                          <SelectItem value="HOME">Home</SelectItem>
                          <SelectItem value="VEHICLE">Vehicle (Auto)</SelectItem>
                          <SelectItem value="PERSONAL">Personal Needs</SelectItem>
                          <SelectItem value="BUSINESS">Business / Startup</SelectItem>
                          <SelectItem value="EDUCATION">Education</SelectItem>
                        </SelectContent>
                      </Select>
                      {errors.loanPurpose && <p className="text-xs text-red-500 pl-1">{errors.loanPurpose}</p>}
                    </div>

                    <InputField id="loanTenure" label="Tenure (Months)" type="number" value={formData.loanTenure} onChange={(v: number) => setFormData({...formData, loanTenure: v})} error={errors.loanTenure} placeholder="e.g. 48" />
                    
                    <div className="p-4 rounded-xl bg-primary/5 flex items-start gap-3 border border-primary/10">
                       <DollarSign className="w-6 h-6 text-primary shrink-0" />
                       <div>
                         <p className="text-sm font-semibold text-foreground">Smart AI Suggestion</p>
                         <p className="text-xs text-muted-foreground mt-1">For ₹{formData.loanAmount || 0}, an optimal tenure is 48 months to keep EMI within 30% of your income.</p>
                       </div>
                    </div>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>

            {error && (
              <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}>
                <Alert variant="destructive" className="glass-card mt-6">
                  <AlertCircle className="h-4 w-4" />
                  <AlertTitle>Submission Error</AlertTitle>
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              </motion.div>
            )}

            <div className="flex justify-between mt-8 pt-6 border-t border-border/40">
              <Button type="button" variant="outline" className="rounded-full px-6 h-12 bg-transparent hover:bg-white/5 border-white/20 transition-all font-medium" onClick={handlePrevious} disabled={isLoading}>
                <ChevronLeft className="h-4 w-4 mr-2" /> Back
              </Button>

              {currentStep < 3 ? (
                <Button type="button" className="rounded-full px-8 h-12 shadow-md shadow-primary/20 hover:shadow-lg transition-all font-medium group" onClick={handleNext}>
                  Continue <ChevronRight className="h-4 w-4 ml-1 group-hover:translate-x-1 transition-transform" />
                </Button>
              ) : (
                <Button type="submit" disabled={isLoading} className="rounded-full px-10 h-12 shadow-lg shadow-primary/30 hover:scale-[1.02] transition-all font-medium group">
                  {isLoading ? (
                    <><Loader2 className="h-5 w-5 animate-spin mr-2" /> Processing...</>
                  ) : (
                    <>Submit Application <CheckCircle2 className="h-4 w-4 ml-2 group-hover:scale-110 transition-transform" /></>
                  )}
                </Button>
              )}
            </div>
          </form>
        </CardContent>
      </Card>

      <Dialog open={isCreditScoreModalOpen} onOpenChange={setIsCreditScoreModalOpen}>
        <DialogContent className="sm:max-w-md bg-slate-900 border-slate-800 text-white rounded-xl">
          <DialogHeader>
            <DialogTitle className="text-xl font-bold">Don't know your credit score?</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4 text-sm text-slate-400">
            <p>You can check it for free using:</p>
            <ul className="list-disc pl-5 space-y-1 text-slate-300">
              <li>CIBIL (official website)</li>
              <li>Paytm app</li>
              <li>OneScore app</li>
              <li>Paisabazaar</li>
            </ul>
            <p className="font-semibold text-white mt-4">Steps:</p>
            <ol className="list-decimal pl-5 space-y-1 text-slate-300">
              <li>Enter your PAN details</li>
              <li>Verify with OTP</li>
              <li>View your credit score instantly</li>
            </ol>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

// Reusable animated input field component
interface InputFieldProps {
  id: string;
  label: string;
  type: string;
  value: number | string;
  onChange: (v: number | string) => void;
  error?: string;
  placeholder?: string;
  onHelpClick?: () => void;
}

const InputField = ({ id, label, type, value, onChange, error, placeholder, onHelpClick }: InputFieldProps) => (
  <div className="space-y-2 group relative">
    <div className="flex items-center gap-2">
      <Label htmlFor={id} className="text-muted-foreground font-medium group-focus-within:text-primary transition-colors">
        {label}
      </Label>
      {onHelpClick && (
        <HelpCircle 
          className="w-4 h-4 text-muted-foreground cursor-pointer hover:text-primary transition-colors" 
          onClick={onHelpClick} 
        />
      )}
    </div>
    <Input
      id={id}
      type={type}
      value={value === 0 ? 0 : (value || '')}
      onChange={(e) => {
        if (e.target.value === '') { onChange(''); return; }
        const parsed = parseFloat(e.target.value);
        onChange(isNaN(parsed) ? 0 : parsed);
      }}
      placeholder={placeholder}
      className="h-12 bg-white/5 border-white/10 hover:border-white/20 focus:border-primary focus:ring-primary/30 rounded-xl transition-all w-full text-foreground pl-4 shadow-sm group-focus-within:shadow-md"
    />
    {error && (
      <motion.p initial={{ opacity: 0, y: -5 }} animate={{ opacity: 1, y: 0 }} className="text-xs text-red-500 pl-1 absolute -bottom-5">
        {error}
      </motion.p>
    )}
  </div>
);