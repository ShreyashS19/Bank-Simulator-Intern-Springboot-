import * as Sentry from "@sentry/react";
import { useState } from "react";

type StatusKind = "idle" | "ok" | "error";

type StatusState = {
  kind: StatusKind;
  message: string;
};

const sentryDsn = import.meta.env.VITE_SENTRY_DSN;
const apiBaseUrl = (import.meta.env.VITE_API_URL || "/api").replace(/\/+$/, "");

const buttonBaseClass =
  "rounded-md border px-2 py-1 text-xs font-medium transition";

export default function SentryTestPanel() {
  const [status, setStatus] = useState<StatusState>({
    kind: "idle",
    message: "",
  });

  const sentryEnabled = Boolean(sentryDsn);

  const sendFrontendError = () => {
    Sentry.captureException(new Error("Sentry frontend test"));
    setStatus({
      kind: "ok",
      message: "Frontend error sent. Check Sentry Issues.",
    });
  };

  const sendBackendError = async () => {
    setStatus({ kind: "idle", message: "" });
    try {
      const response = await fetch(`${apiBaseUrl}/auth/test-sentry`, {
        method: "GET",
      });
      if (!response.ok) {
        throw new Error(`Backend responded with ${response.status}`);
      }
      setStatus({
        kind: "ok",
        message: "Backend error triggered. Check Sentry Issues.",
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unknown error";
      setStatus({ kind: "error", message: `Backend test failed: ${message}` });
    }
  };

  return (
    <div className="fixed bottom-4 right-4 z-50 w-72 rounded-md border bg-white/95 p-3 text-xs shadow-lg backdrop-blur">
      <div className="mb-2 font-semibold text-slate-900">Sentry Test Panel</div>
      <div className="mb-2 text-slate-600">
        {sentryEnabled
          ? "Sentry DSN loaded."
          : "Sentry DSN missing (set VITE_SENTRY_DSN)."}
      </div>
      <div className="flex flex-col gap-2">
        <button
          className={`${buttonBaseClass} ${
            sentryEnabled
              ? "border-slate-200 bg-slate-900 text-white hover:bg-slate-800"
              : "border-slate-200 bg-slate-100 text-slate-400"
          }`}
          disabled={!sentryEnabled}
          onClick={sendFrontendError}
          type="button"
        >
          Send frontend error
        </button>
        <button
          className={`${buttonBaseClass} border-slate-200 bg-white text-slate-700 hover:bg-slate-50`}
          onClick={sendBackendError}
          type="button"
        >
          Trigger backend error
        </button>
      </div>
      {status.message && (
        <div
          className={`mt-2 ${
            status.kind === "error" ? "text-red-600" : "text-green-700"
          }`}
        >
          {status.message}
        </div>
      )}
    </div>
  );
}
