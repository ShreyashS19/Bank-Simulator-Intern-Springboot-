import { useState } from "react";
import * as Sentry from "@sentry/react";
import { Button } from "@/components/ui/button";
import { Bug, AlertTriangle, Send, X, ShieldAlert } from "lucide-react";

export function ReportBugButton() {
  const [isOpen, setIsOpen] = useState(false);
  const [shouldCrash, setShouldCrash] = useState(false);

  if (shouldCrash) {
    throw new Error("Simulated Frontend Crash - Bank Simulator Test");
  }

  const handleReport = () => {
    // 1. Capture a Sentry message first to generate an eventId
    const eventId = Sentry.captureMessage("User feedback requested via floating button");
    
    // 2. Open the Sentry report dialog linked directly to this event ID
    Sentry.showReportDialog({
      eventId,
      title: "Report a Bug",
      subtitle: "Our team will look into this.",
      subtitle2: "Please describe what happened.",
      labelName: "Your Name",
      labelEmail: "Your Email",
      labelComments: "What went wrong?",
      labelClose: "Close",
      labelSubmit: "Submit Report",
    });
    setIsOpen(false);
  };

  const handleCrash = () => {
    handleReport(); // Also open report dialog or just crash? Let's just crash so the global error boundary does it!
    setShouldCrash(true);
  };

  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col items-end gap-2">
      {/* Glassmorphic Dropdown Menu */}
      {isOpen && (
        <div className="mb-2 w-64 overflow-hidden rounded-2xl border border-white/10 bg-black/60 p-4 text-white backdrop-blur-xl shadow-2xl transition-all duration-300 animate-in fade-in slide-in-from-bottom-5">
          <div className="flex items-center justify-between border-b border-white/10 pb-2 mb-3">
            <span className="flex items-center gap-2 text-sm font-semibold text-cyan-400">
              <ShieldAlert className="h-4 w-4" />
              Sentry Diagnostics
            </span>
            <button
              onClick={() => setIsOpen(false)}
              className="rounded-full p-1 text-white/60 hover:bg-white/10 hover:text-white"
            >
              <X className="h-3.5 w-3.5" />
            </button>
          </div>
          
          <div className="flex flex-col gap-2">
            <button
              onClick={handleReport}
              className="flex w-full items-center gap-3 rounded-lg bg-cyan-500/20 px-3 py-2.5 text-left text-xs font-medium text-cyan-200 border border-cyan-500/30 transition-all hover:bg-cyan-500/30 hover:border-cyan-500/50"
            >
              <Send className="h-4 w-4 text-cyan-400" />
              <div>
                <p className="font-semibold">Submit Bug Report</p>
                <p className="text-[10px] text-cyan-300/70">Opens Sentry feedback dialog</p>
              </div>
            </button>

            <button
              onClick={handleCrash}
              className="flex w-full items-center gap-3 rounded-lg bg-red-500/20 px-3 py-2.5 text-left text-xs font-medium text-red-200 border border-red-500/30 transition-all hover:bg-red-500/30 hover:border-red-500/50"
            >
              <AlertTriangle className="h-4 w-4 text-red-400" />
              <div>
                <p className="font-semibold text-red-300">Test Unhandled Crash</p>
                <p className="text-[10px] text-red-300/70">Triggers global ErrorBoundary</p>
              </div>
            </button>
          </div>
        </div>
      )}

      {/* Main Floating Trigger Button */}
      <Button
        variant="outline"
        size="sm"
        onClick={() => setIsOpen(!isOpen)}
        className="gap-2 rounded-full border border-white/10 bg-black/60 text-white backdrop-blur-xl shadow-lg transition-all duration-300 hover:scale-105 hover:border-cyan-500/50 hover:bg-black/80"
      >
        <Bug className="h-4 w-4 text-cyan-400" />
        Sentry Tests
      </Button>
    </div>
  );
}
