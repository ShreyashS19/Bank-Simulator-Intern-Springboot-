import * as Sentry from "@sentry/react";
import { createRoot } from "react-dom/client";
import App from "./App.tsx";
import "./index.css";

Sentry.init({
  dsn: import.meta.env.VITE_SENTRY_DSN,
  integrations: [
    Sentry.browserTracingIntegration(),
    Sentry.replayIntegration({
      maskAllText: false,
      blockAllMedia: false,
    }),
  ],
  tracesSampleRate: 1.0,
  tracePropagationTargets: ["localhost", /^https?:\/\/your-backend-domain/],
  replaysSessionSampleRate: 0.1,
  replaysOnErrorSampleRate: 1.0,
  environment: import.meta.env.MODE,
  sendDefaultPii: true,
  debug: true, // Output SDK logs to browser developer console (F12)
});

// ─── Smart Sentry Console Error Interceptor ───────────────────────────
// Captures swallowed dashboard & API errors logged via console.error in try-catch blocks.
const originalConsoleError = console.error;
console.error = (...args) => {
  // Find a real error object in the arguments
  const errorObj = args.find((arg) => arg instanceof Error || (arg && typeof arg === 'object' && 'message' in arg));
  
  if (errorObj) {
    const contextText = args.filter((arg) => arg !== errorObj).join(' ');
    Sentry.captureException(errorObj, {
      extra: {
        context: contextText || "Logged via console.error",
      },
      tags: {
        capturedVia: "console.error_interceptor",
      }
    });
  } else {
    const message = args.join(' ');
    const isRealError = message.toLowerCase().includes('failed') || 
                        message.toLowerCase().includes('error') || 
                        message.toLowerCase().includes('exception');
    const isReactWarning = message.includes('React') || message.includes('component') || message.includes('prop');
    
    if (isRealError && !isReactWarning) {
      Sentry.captureMessage(message, {
        level: "error",
        tags: {
          capturedVia: "console.error_interceptor",
        }
      });
    }
  }

  // Always invoke original console.error to keep standard console logging
  originalConsoleError.apply(console, args);
};

createRoot(document.getElementById("root")!).render(<App />);