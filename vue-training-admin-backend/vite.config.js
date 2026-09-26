import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    // 开发服务器: npm run dev
    host: 'localhost',
    port: 5173,
    // 启动后自动打开浏览器, 省得你以为没跑起来
    open: true,
    proxy: {
      // 前端统一请求 /api/xxx, 这里把 /api 去掉后转发到后端 8080
      // 例: /api/employees        -> http://localhost:8080/employees
      //     /api/uploads/xxx.jpg  -> http://localhost:8080/uploads/xxx.jpg
      //     /api/files/upload     -> http://localhost:8080/files/upload
      '/api': {
        target: 'http://localhost:8080',
        secure: false,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      }
    }
  },
  preview: {
    port: 4173
  },
  build: {
    // 构建产物输出到 dist/
    chunkSizeWarningLimit: 3000
  }
})
