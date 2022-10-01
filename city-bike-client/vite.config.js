import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [
    vue(),
    {apply: 'build', enforce: 'pre'}
  ],
  build: {
    rollupOptions: {
      input: './index.html',
    }
  },
  server: {
    port: 8081
  }
})
