import React, { useCallback, useEffect, useRef, useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { DotLottieReact } from '@lottiefiles/dotlottie-react';
import type { DotLottie } from '@lottiefiles/dotlottie-react';
import { IndianRupee, RefreshCcw, ShieldCheck, X } from 'lucide-react';

// ─── Animation src ────────────────────────────────────────────────────────
const SUCCESS_LOTTIE =
  'https://lottie.host/f2ece777-1ec3-4911-8ac1-2949473279ab/kGCONV1NwS.lottie';

// ─── Lightweight Web-Audio SFX (zero external files) ─────────────────────
export function playSFX(type: 'tick' | 'success' | 'error') {
  try {
    const Ctx = window.AudioContext ?? (window as any).webkitAudioContext;
    if (!Ctx) return;
    const ctx = new Ctx();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.connect(gain);
    gain.connect(ctx.destination);
    const t = ctx.currentTime;

    if (type === 'tick') {
      osc.type = 'sine';
      osc.frequency.setValueAtTime(880, t);
      gain.gain.setValueAtTime(0.4, t);
      gain.gain.exponentialRampToValueAtTime(0.001, t + 0.08);
      osc.start(t); osc.stop(t + 0.08);
    } else if (type === 'success') {
      osc.type = 'triangle';
      ([523.25, 659.25, 1046.5] as number[]).forEach((freq, i) => {
        osc.frequency.setValueAtTime(freq, t + i * 0.12);
      });
      gain.gain.setValueAtTime(0, t);
      gain.gain.linearRampToValueAtTime(0.28, t + 0.05);
      gain.gain.linearRampToValueAtTime(0, t + 0.55);
      osc.start(t); osc.stop(t + 0.6);
    } else {
      osc.type = 'sawtooth';
      osc.frequency.setValueAtTime(160, t);
      osc.frequency.linearRampToValueAtTime(80, t + 0.35);
      gain.gain.setValueAtTime(0.28, t);
      gain.gain.exponentialRampToValueAtTime(0.001, t + 0.35);
      osc.start(t); osc.stop(t + 0.35);
    }
  } catch (_) { /* ignore non-interactive contexts */ }
}

// ─── Confetti burst ───────────────────────────────────────────────────────
const COLORS = ['#10B981', '#3B82F6', '#F59E0B', '#A855F7', '#EC4899'];

function Confetti() {
  return (
    <div className="pointer-events-none absolute inset-0 overflow-hidden">
      {Array.from({ length: 36 }).map((_, i) => (
        <motion.span
          key={i}
          className="absolute left-1/2 top-1/2 h-2 w-2 rounded-full"
          style={{ backgroundColor: COLORS[i % COLORS.length] }}
          initial={{ x: 0, y: 0, scale: 0, opacity: 1 }}
          animate={{
            x: (Math.random() - 0.5) * 500,
            y: (Math.random() - 0.5) * 500,
            scale: Math.random() * 1.4 + 0.3,
            opacity: 0,
          }}
          transition={{ duration: 0.9 + Math.random() * 0.6, ease: 'easeOut' }}
        />
      ))}
    </div>
  );
}

// ─── Step progress bar ────────────────────────────────────────────────────
const STEPS = ['Initiating', 'Processing', 'Finalizing'] as const;
export type Step = (typeof STEPS)[number];

function StepBar({ step }: { step: Step }) {
  const idx = STEPS.indexOf(step);
  return (
    <div className="w-full max-w-xs">
      <div className="mb-2 flex justify-between px-1 text-xs font-medium text-gray-400">
        {STEPS.map((s, i) => (
          <span key={s} className={i <= idx ? 'text-blue-600' : ''}>{s}</span>
        ))}
      </div>
      <div className="h-1.5 w-full overflow-hidden rounded-full bg-gray-100">
        <motion.div
          className="relative h-full overflow-hidden rounded-full bg-blue-600"
          initial={{ width: '0%' }}
          animate={{ width: `${((idx + 1) / STEPS.length) * 100}%` }}
          transition={{ duration: 0.55, ease: 'easeInOut' }}
        >
          <motion.div
            className="absolute inset-0 bg-gradient-to-r from-transparent via-white/40 to-transparent"
            animate={{ x: ['-100%', '200%'] }}
            transition={{ duration: 1.4, repeat: Infinity, ease: 'linear' }}
          />
        </motion.div>
      </div>
    </div>
  );
}

// ─── Processing view ──────────────────────────────────────────────────────
function ProcessingView({ step }: { step: Step }) {
  return (
    <motion.div
      key="processing"
      initial={{ opacity: 0, scale: 0.94 }}
      animate={{ opacity: 1, scale: 1 }}
      exit={{ opacity: 0, scale: 0.94, filter: 'blur(4px)' }}
      className="flex flex-col items-center py-8"
    >
      <div className="relative mb-8 flex h-24 w-24 items-center justify-center">
        <div className="absolute inset-0 rounded-full border-4 border-gray-100" />
        <motion.div
          className="absolute inset-0 rounded-full border-4 border-t-transparent border-blue-600"
          animate={{ rotate: 360 }}
          transition={{ duration: 0.9, repeat: Infinity, ease: 'linear' }}
        />
        <ShieldCheck className="h-9 w-9 animate-pulse text-blue-600" />
      </div>
      <h3 className="mb-1 text-xl font-semibold text-gray-800">
        Processing your payment…
      </h3>
      <p className="mb-8 max-w-xs text-center text-sm text-gray-500">
        Please don't close this window or press back.
      </p>
      <StepBar step={step} />
    </motion.div>
  );
}

// ─── Success view — uses dotLottieRefCallback for onComplete ──────────────
function SuccessView({
  amount,
  receiver,
  txnId,
  onDone,
}: {
  amount: string | number;
  receiver: string;
  txnId?: string;
  onDone: () => void;
}) {
  const [animDone, setAnimDone] = useState(false);
  const autoCloseRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const instanceRef = useRef<DotLottie | null>(null);

  // Called by DotLottieReact when the underlying DotLottie instance is ready
  const handleRefCallback = useCallback((instance: DotLottie | null) => {
    if (instanceRef.current) {
      // clean up previous listeners when component remounts
      instanceRef.current.removeEventListener('complete', handleComplete);
    }
    instanceRef.current = instance;
    if (instance) {
      instance.addEventListener('complete', handleComplete);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function handleComplete() {
    setAnimDone(true);
    autoCloseRef.current = setTimeout(onDone, 2500);
  }

  // Safety fallback in case CDN is slow / CORS blocks the complete event
  useEffect(() => {
    const fallback = setTimeout(() => setAnimDone(true), 4000);
    return () => {
      clearTimeout(fallback);
      if (autoCloseRef.current) clearTimeout(autoCloseRef.current);
      // Remove listener on unmount
      instanceRef.current?.removeEventListener('complete', handleComplete);
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <motion.div
      key="success"
      initial={{ opacity: 0, scale: 0.92 }}
      animate={{ opacity: 1, scale: 1 }}
      className="relative flex flex-col items-center"
    >
      <Confetti />

      {/* Lottie player with glow rings */}
      <div className="relative z-10 h-44 w-44">
        <motion.div
          className="absolute inset-0 rounded-full bg-emerald-400/20"
          animate={{ scale: [1, 1.35, 1], opacity: [0.6, 0.15, 0.6] }}
          transition={{ duration: 2.2, repeat: Infinity, ease: 'easeInOut' }}
        />
        <motion.div
          className="absolute -inset-4 rounded-full bg-emerald-300/10"
          animate={{ scale: [1, 1.5, 1], opacity: [0.4, 0.08, 0.4] }}
          transition={{ duration: 2.8, repeat: Infinity, ease: 'easeInOut', delay: 0.4 }}
        />
        <DotLottieReact
          src={SUCCESS_LOTTIE}
          autoplay
          loop={false}
          dotLottieRefCallback={handleRefCallback}
          style={{ width: '100%', height: '100%', position: 'relative', zIndex: 10 }}
        />
      </div>

      {/* Content revealed after animation completes */}
      <AnimatePresence>
        {animDone && (
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.45, ease: 'easeOut' }}
            className="z-10 mt-2 w-full"
          >
            <div className="mb-5 text-center">
              <h2 className="text-2xl font-bold text-gray-800">
                Payment Successful 🎉
              </h2>
              <p className="mt-1 text-sm text-gray-500">
                Money transferred securely
              </p>
            </div>

            {/* Receipt card */}
            <div className="mb-6 rounded-2xl border border-gray-100 bg-gray-50 p-5">
              <div className="mb-4 flex items-center justify-between border-b border-gray-200 pb-4">
                <span className="text-sm text-gray-500">Amount Paid</span>
                <span className="flex items-center gap-1 text-2xl font-bold text-gray-800">
                  <IndianRupee className="h-5 w-5" />
                  {amount}
                </span>
              </div>
              <div className="mb-3 flex items-center justify-between">
                <span className="text-sm text-gray-500">Paid To</span>
                <span className="font-medium text-gray-800">{receiver}</span>
              </div>
              {txnId && (
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-500">Transaction ID</span>
                  <span className="rounded bg-gray-200/60 px-2 py-0.5 font-mono text-xs font-semibold text-gray-700">
                    {txnId}
                  </span>
                </div>
              )}
            </div>

            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.97 }}
              onClick={onDone}
              className="w-full rounded-xl bg-gray-900 py-4 font-semibold text-white shadow-lg shadow-gray-900/20 transition-shadow hover:shadow-gray-900/30"
            >
              Done
            </motion.button>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
}

// ─── Error view ───────────────────────────────────────────────────────────
function ErrorView({
  message,
  onRetry,
  onCancel,
}: {
  message: string;
  onRetry: () => void;
  onCancel: () => void;
}) {
  return (
    <motion.div
      key="error"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1, x: [0, -10, 10, -10, 10, 0] }}
      transition={{ duration: 0.45 }}
      className="flex flex-col items-center py-6"
    >
      <div className="relative mb-6 flex h-24 w-24 items-center justify-center rounded-full bg-red-50">
        <motion.div className="absolute inset-0 animate-ping rounded-full border border-red-400/30" />
        <X className="h-12 w-12 text-red-500" strokeWidth={2.5} />
      </div>

      <h2 className="mb-2 text-2xl font-bold text-gray-800">Payment Failed</h2>
      <p className="mb-8 max-w-xs text-center text-sm text-gray-500">
        {message || "We couldn't process your payment. No money was deducted."}
      </p>

      <div className="w-full space-y-3">
        <motion.button
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.97 }}
          onClick={onRetry}
          className="flex w-full items-center justify-center gap-2 rounded-xl bg-blue-600 py-4 font-semibold text-white shadow-lg shadow-blue-600/25"
        >
          <RefreshCcw className="h-5 w-5" />
          Retry Payment
        </motion.button>
        <motion.button
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.97 }}
          onClick={onCancel}
          className="w-full rounded-xl bg-gray-100 py-4 font-semibold text-gray-700 transition-colors hover:bg-gray-200"
        >
          Cancel
        </motion.button>
      </div>
    </motion.div>
  );
}

// ─── Public types & root modal ────────────────────────────────────────────
export interface PaymentModalProps {
  isOpen: boolean;
  status: 'processing' | 'success' | 'error';
  step?: Step;
  paymentDetails: { amount: string | number; receiver: string; txnId?: string };
  errorMessage?: string;
  onClose: () => void;
  onRetry: () => void;
}

export function PaymentModal({
  isOpen,
  status,
  step = 'Initiating',
  paymentDetails,
  errorMessage,
  onClose,
  onRetry,
}: PaymentModalProps) {
  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-[200] flex items-center justify-center p-4">
        {/* Backdrop */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="absolute inset-0 bg-black/45 backdrop-blur-sm"
          onClick={status !== 'processing' ? onClose : undefined}
        />

        {/* Card */}
        <motion.div
          initial={{ opacity: 0, y: 48, scale: 0.94 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={{ opacity: 0, y: 24, scale: 0.95 }}
          transition={{ type: 'spring', damping: 26, stiffness: 320 }}
          className="relative z-10 w-full max-w-sm overflow-hidden rounded-[2rem] bg-white p-8 shadow-2xl"
        >
          <AnimatePresence mode="wait">
            {status === 'processing' && <ProcessingView key="proc" step={step} />}
            {status === 'success' && (
              <SuccessView
                key="succ"
                amount={paymentDetails.amount}
                receiver={paymentDetails.receiver}
                txnId={paymentDetails.txnId}
                onDone={onClose}
              />
            )}
            {status === 'error' && (
              <ErrorView
                key="err"
                message={errorMessage ?? ''}
                onRetry={onRetry}
                onCancel={onClose}
              />
            )}
          </AnimatePresence>

          {/* Footer badge */}
          <div className="absolute bottom-3 left-0 right-0 flex items-center justify-center gap-1 opacity-35">
            <ShieldCheck className="h-3.5 w-3.5" />
            <span className="text-[10px] font-semibold uppercase tracking-widest">
              256-bit Encrypted
            </span>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
}
