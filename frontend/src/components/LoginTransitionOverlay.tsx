import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ShieldCheck, Landmark } from 'lucide-react';

/**
 * Full-screen branded transition overlay shown right after a successful login.
 * It plays its animation and then calls onComplete() so the caller can navigate.
 *
 * Usage:
 *   <LoginTransitionOverlay userName="Rahul" onComplete={() => navigate('/dashboard')} />
 */
interface Props {
  userName?: string;
  onComplete: () => void;
}

export default function LoginTransitionOverlay({ userName, onComplete }: Props) {
  const [phase, setPhase] = useState<'enter' | 'greet' | 'exit'>('enter');

  useEffect(() => {
    // Phase timeline:
    //  0ms  → 'enter'  (logo + ring zoom in)
    //  700ms → 'greet' (greeting text slides up)
    //  2000ms → 'exit' (whole screen wipes upward revealing dashboard)

    const t1 = setTimeout(() => setPhase('greet'), 700);
    const t2 = setTimeout(() => setPhase('exit'), 2100);
    const t3 = setTimeout(onComplete, 2700);   // navigate after wipe completes

    return () => {
      clearTimeout(t1);
      clearTimeout(t2);
      clearTimeout(t3);
    };
  }, [onComplete]);

  return (
    <AnimatePresence>
      {phase !== 'exit' ? (
        <motion.div
          key="overlay-body"
          className="fixed inset-0 z-[9999] flex flex-col items-center justify-center overflow-hidden"
          style={{
            background: 'linear-gradient(135deg, #0f172a 0%, #1e3a5f 50%, #0f2744 100%)',
          }}
          initial={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.5 }}
        >
          {/* ── Animated background blobs ── */}
          <motion.div
            className="absolute -top-32 -left-32 w-[500px] h-[500px] rounded-full opacity-20"
            style={{ background: 'radial-gradient(circle, #3b82f6, transparent 70%)' }}
            animate={{ scale: [1, 1.15, 1], rotate: [0, 20, 0] }}
            transition={{ duration: 6, repeat: Infinity, ease: 'easeInOut' }}
          />
          <motion.div
            className="absolute -bottom-40 -right-40 w-[600px] h-[600px] rounded-full opacity-10"
            style={{ background: 'radial-gradient(circle, #60a5fa, transparent 70%)' }}
            animate={{ scale: [1, 1.2, 1], rotate: [0, -15, 0] }}
            transition={{ duration: 8, repeat: Infinity, ease: 'easeInOut', delay: 1 }}
          />

          {/* Floating particle dots */}
          {[...Array(12)].map((_, i) => (
            <motion.div
              key={i}
              className="absolute rounded-full bg-blue-400/30"
              style={{
                width: Math.random() * 6 + 3,
                height: Math.random() * 6 + 3,
                left: `${Math.random() * 100}%`,
                top: `${Math.random() * 100}%`,
              }}
              animate={{
                y: [0, -30, 0],
                opacity: [0.3, 0.8, 0.3],
              }}
              transition={{
                duration: 2 + Math.random() * 3,
                repeat: Infinity,
                delay: Math.random() * 2,
                ease: 'easeInOut',
              }}
            />
          ))}

          {/* ── Logo + pulsing ring ── */}
          <motion.div
            className="relative flex items-center justify-center mb-8"
            initial={{ scale: 0, rotate: -180 }}
            animate={{ scale: 1, rotate: 0 }}
            transition={{ type: 'spring', stiffness: 200, damping: 18, duration: 0.7 }}
          >
            {/* Outer pulsing ring */}
            <motion.div
              className="absolute w-32 h-32 rounded-full border-2 border-blue-400/40"
              animate={{ scale: [1, 1.4, 1], opacity: [0.7, 0.1, 0.7] }}
              transition={{ duration: 2, repeat: Infinity, ease: 'easeInOut' }}
            />
            {/* Middle ring */}
            <motion.div
              className="absolute w-24 h-24 rounded-full border border-blue-300/30"
              animate={{ scale: [1, 1.3, 1], opacity: [0.5, 0.05, 0.5] }}
              transition={{ duration: 2, repeat: Infinity, ease: 'easeInOut', delay: 0.3 }}
            />
            {/* Icon circle */}
            <div className="relative z-10 w-20 h-20 rounded-full bg-gradient-to-br from-blue-500 to-blue-700 flex items-center justify-center shadow-[0_0_40px_rgba(59,130,246,0.5)]">
              <Landmark className="w-9 h-9 text-white" />
            </div>
          </motion.div>

          {/* ── Brand name ── */}
          <motion.h1
            className="text-3xl font-bold text-white tracking-wider mb-1"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4, duration: 0.5 }}
          >
            Bank Simulation
          </motion.h1>

          {/* ── Progress bar ── */}
          <motion.div
            className="w-48 h-1 bg-white/10 rounded-full mt-6 overflow-hidden"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.5 }}
          >
            <motion.div
              className="h-full rounded-full bg-gradient-to-r from-blue-400 to-cyan-300"
              initial={{ width: '0%' }}
              animate={{ width: '100%' }}
              transition={{ duration: 1.6, delay: 0.6, ease: 'easeInOut' }}
            />
          </motion.div>

          {/* ── Greeting text (phase 2) ── */}
          <AnimatePresence>
            {phase === 'greet' && (
              <motion.div
                className="absolute bottom-20 flex flex-col items-center gap-1"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.5, ease: 'easeOut' }}
              >
                <div className="flex items-center gap-2 text-blue-300/80 text-sm font-medium tracking-widest uppercase">
                  <ShieldCheck className="w-4 h-4" />
                  <span>Authentication Successful</span>
                </div>
                {userName && (
                  <p className="text-white/60 text-sm mt-1">
                    Welcome back,{' '}
                    <span className="text-white font-semibold">{userName}</span>
                  </p>
                )}
              </motion.div>
            )}
          </AnimatePresence>
        </motion.div>
      ) : (
        /* ── EXIT: curtain wipe upward ── */
        <motion.div
          key="overlay-exit"
          className="fixed inset-0 z-[9999]"
          style={{
            background: 'linear-gradient(135deg, #0f172a 0%, #1e3a5f 50%, #0f2744 100%)',
          }}
          initial={{ y: 0 }}
          animate={{ y: '-100%' }}
          transition={{ duration: 0.65, ease: [0.76, 0, 0.24, 1] }}
        />
      )}
    </AnimatePresence>
  );
}
