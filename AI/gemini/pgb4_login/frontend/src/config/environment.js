/**
 * Environment configuration utility
 * Centralizes all environment variable handling for the frontend application
 */

// API Configuration
export const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  TIMEOUT: parseInt(import.meta.env.VITE_API_TIMEOUT) || 10000,
};

// Application Configuration
export const APP_CONFIG = {
  NAME: import.meta.env.VITE_APP_NAME || 'PGB4 Message Board',
  VERSION: import.meta.env.VITE_APP_VERSION || '1.0.0',
  ENV: import.meta.env.VITE_APP_ENV || import.meta.env.MODE || 'development',
};

// Build Configuration
export const BUILD_CONFIG = {
  SOURCEMAP: import.meta.env.VITE_BUILD_SOURCEMAP === 'true',
  MINIFY: import.meta.env.VITE_BUILD_MINIFY !== 'false',
};

// Development helpers
export const isDevelopment = () => APP_CONFIG.ENV === 'development';
export const isProduction = () => APP_CONFIG.ENV === 'production';

// Environment validation
export const validateEnvironment = () => {
  const requiredVars = ['VITE_API_BASE_URL'];
  const missing = requiredVars.filter(varName => !import.meta.env[varName]);
  
  if (missing.length > 0) {
    console.warn('Missing environment variables:', missing);
    if (isProduction()) {
      throw new Error(`Missing required environment variables: ${missing.join(', ')}`);
    }
  }
  
  return true;
};

// Log configuration in development
if (isDevelopment()) {
  console.log('Environment Configuration:', {
    API_CONFIG,
    APP_CONFIG,
    BUILD_CONFIG,
  });
}