import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import path from 'path'


export default defineConfig(({ mode }) => ({
  server: {
    host: '0.0.0.0',
    port: 5173,  // Changed from 8080 to avoid conflict with Tomcat
    proxy: {
      '/api': {
        target: 'http://localhost:8080/bank-simulator',
        changeOrigin: true,
        secure: false,
      }
    }
  },
  plugins: [
    react(),
  ].filter(Boolean),
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
}))
