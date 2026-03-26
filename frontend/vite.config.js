import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
proxy: {
      '/TradeX/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/TradeX/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      },
      '/yahoo': {
        target: 'http://localhost:5002',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/yahoo/, ''),
      },
    },
  },
})
