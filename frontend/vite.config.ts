import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import path from 'path'

export default defineConfig(({ mode }) => ({
  server: {
    // FIX: Don't bind to 0.0.0.0 in development — localhost only
    host: 'localhost',
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080/bank-simulator',
        changeOrigin: true,
        // FIX: secure: false is fine for local http, but document it explicitly
        secure: false,
      }
    }
  },

  // FIX: Add Content Security Policy and other security headers for the dev server
  // (In production these should be set by the reverse proxy / Tomcat)
  ...(mode === 'development' && {
    preview: {
      headers: {
        'X-Content-Type-Options': 'nosniff',
        'X-Frame-Options': 'DENY',
        'Referrer-Policy': 'strict-origin-when-cross-origin',
      }
    }
  }),

  plugins: [
    react(),
  ].filter(Boolean),

  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },

  // FIX: Don't expose source maps in production — leaks internal logic
  build: {
    sourcemap: mode === 'development',
    // FIX: Chunk size warning threshold
    chunkSizeWarningLimit: 600,
  },

  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    css: true,
  },
}))