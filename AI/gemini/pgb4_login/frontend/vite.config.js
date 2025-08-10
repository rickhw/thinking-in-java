import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ command, mode }) => {
  // Load env file based on `mode` in the current working directory.
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    plugins: [react()],
    
    // Environment variables configuration
    define: {
      __APP_VERSION__: JSON.stringify(env.VITE_APP_VERSION || '1.0.0'),
      __APP_NAME__: JSON.stringify(env.VITE_APP_NAME || 'PGB4 Message Board'),
    },
    
    // Build configuration
    build: {
      // Generate sourcemap based on environment
      sourcemap: env.VITE_BUILD_SOURCEMAP === 'true',
      
      // Minification settings
      minify: env.VITE_BUILD_MINIFY !== 'false',
      
      // Chunk size warning limit
      chunkSizeWarningLimit: parseInt(env.VITE_CHUNK_SIZE_WARNING_LIMIT) || 500,
      
      // Rollup options for optimization
      rollupOptions: {
        output: {
          // Manual chunks for better caching
          manualChunks: {
            vendor: ['react', 'react-dom'],
            router: ['react-router-dom'],
            utils: ['axios']
          }
        }
      },
      
      // Target modern browsers for production
      target: mode === 'production' ? 'es2015' : 'esnext',
    },
    
    // Server configuration for development
    server: {
      port: 3000,
      host: true,
      // Proxy API requests to backend during development
      proxy: mode === 'development' ? {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '/api')
        }
      } : undefined
    },
    
    // Preview server configuration
    preview: {
      port: 3000,
      host: true
    }
  }
})
