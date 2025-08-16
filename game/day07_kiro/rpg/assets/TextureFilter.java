package rpg.assets;

import rpg.utils.GameLogger;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Texture filtering and scaling utilities for sprite management.
 * Provides various filtering options for texture scaling and enhancement.
 */
public class TextureFilter {
    
    /**
     * Filtering modes for texture scaling.
     */
    public enum FilterMode {
        NEAREST_NEIGHBOR,
        BILINEAR,
        BICUBIC,
        AREA_AVERAGING
    }
    
    /**
     * Scale a texture using the specified filter mode.
     */
    public static BufferedImage scaleTexture(BufferedImage source, int newWidth, int newHeight, FilterMode filterMode) {
        if (source == null) {
            GameLogger.warn("Cannot scale null texture");
            return null;
        }
        
        if (newWidth <= 0 || newHeight <= 0) {
            GameLogger.warn("Invalid scale dimensions: " + newWidth + "x" + newHeight);
            return source;
        }
        
        // If dimensions are the same, return original
        if (source.getWidth() == newWidth && source.getHeight() == newHeight) {
            return source;
        }
        
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, source.getType());
        Graphics2D g2d = scaledImage.createGraphics();
        
        // Set rendering hints based on filter mode
        setRenderingHints(g2d, filterMode);
        
        // Scale the image
        g2d.drawImage(source, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        GameLogger.debug("Scaled texture from " + source.getWidth() + "x" + source.getHeight() + 
                    " to " + newWidth + "x" + newHeight + " using " + filterMode);
        
        return scaledImage;
    }
    
    /**
     * Scale a texture by a factor.
     */
    public static BufferedImage scaleTexture(BufferedImage source, float scaleFactor, FilterMode filterMode) {
        if (source == null || scaleFactor <= 0) {
            return source;
        }
        
        int newWidth = Math.round(source.getWidth() * scaleFactor);
        int newHeight = Math.round(source.getHeight() * scaleFactor);
        
        return scaleTexture(source, newWidth, newHeight, filterMode);
    }
    
    /**
     * Create a pixel-perfect scaled version (integer scaling only).
     */
    public static BufferedImage scalePixelPerfect(BufferedImage source, int scale) {
        if (source == null || scale <= 0) {
            return source;
        }
        
        if (scale == 1) {
            return source;
        }
        
        int newWidth = source.getWidth() * scale;
        int newHeight = source.getHeight() * scale;
        
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, source.getType());
        
        // Manual pixel scaling for perfect pixel art scaling
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int pixel = source.getRGB(x, y);
                
                // Fill the scaled block
                for (int sy = 0; sy < scale; sy++) {
                    for (int sx = 0; sx < scale; sx++) {
                        scaledImage.setRGB(x * scale + sx, y * scale + sy, pixel);
                    }
                }
            }
        }
        
        GameLogger.debug("Pixel-perfect scaled texture by " + scale + "x");
        return scaledImage;
    }
    
    /**
     * Apply a color filter to a texture.
     */
    public static BufferedImage applyColorFilter(BufferedImage source, Color filterColor, float intensity) {
        if (source == null || intensity <= 0) {
            return source;
        }
        
        BufferedImage filtered = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics2D g2d = filtered.createGraphics();
        
        // Draw original image
        g2d.drawImage(source, 0, 0, null);
        
        // Apply color overlay
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, intensity));
        g2d.setColor(filterColor);
        g2d.fillRect(0, 0, source.getWidth(), source.getHeight());
        
        g2d.dispose();
        
        GameLogger.debug("Applied color filter: " + filterColor + " with intensity " + intensity);
        return filtered;
    }
    
    /**
     * Create a grayscale version of a texture.
     */
    public static BufferedImage toGrayscale(BufferedImage source) {
        if (source == null) {
            return null;
        }
        
        BufferedImage grayscale = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayscale.createGraphics();
        g2d.drawImage(source, 0, 0, null);
        g2d.dispose();
        
        GameLogger.debug("Converted texture to grayscale");
        return grayscale;
    }
    
    /**
     * Adjust brightness of a texture.
     */
    public static BufferedImage adjustBrightness(BufferedImage source, float brightness) {
        if (source == null) {
            return null;
        }
        
        BufferedImage adjusted = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                
                int alpha = (rgb >> 24) & 0xFF;
                int red = Math.min(255, Math.max(0, (int) (((rgb >> 16) & 0xFF) * brightness)));
                int green = Math.min(255, Math.max(0, (int) (((rgb >> 8) & 0xFF) * brightness)));
                int blue = Math.min(255, Math.max(0, (int) ((rgb & 0xFF) * brightness)));
                
                int newRgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                adjusted.setRGB(x, y, newRgb);
            }
        }
        
        GameLogger.debug("Adjusted texture brightness by factor " + brightness);
        return adjusted;
    }
    
    /**
     * Create a flipped version of a texture.
     */
    public static BufferedImage flipTexture(BufferedImage source, boolean horizontal, boolean vertical) {
        if (source == null) {
            return null;
        }
        
        if (!horizontal && !vertical) {
            return source;
        }
        
        BufferedImage flipped = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics2D g2d = flipped.createGraphics();
        
        int x = horizontal ? source.getWidth() : 0;
        int y = vertical ? source.getHeight() : 0;
        int width = horizontal ? -source.getWidth() : source.getWidth();
        int height = vertical ? -source.getHeight() : source.getHeight();
        
        g2d.drawImage(source, x, y, width, height, null);
        g2d.dispose();
        
        GameLogger.debug("Flipped texture - horizontal: " + horizontal + ", vertical: " + vertical);
        return flipped;
    }
    
    /**
     * Rotate a texture by 90-degree increments.
     */
    public static BufferedImage rotateTexture(BufferedImage source, int degrees) {
        if (source == null) {
            return null;
        }
        
        // Normalize degrees to 0, 90, 180, 270
        degrees = ((degrees % 360) + 360) % 360;
        if (degrees % 90 != 0) {
            GameLogger.warn("Rotation degrees must be multiples of 90. Got: " + degrees);
            return source;
        }
        
        if (degrees == 0) {
            return source;
        }
        
        int width = source.getWidth();
        int height = source.getHeight();
        
        BufferedImage rotated;
        Graphics2D g2d;
        
        switch (degrees) {
            case 90:
                rotated = new BufferedImage(height, width, source.getType());
                g2d = rotated.createGraphics();
                g2d.rotate(Math.toRadians(90), height / 2.0, height / 2.0);
                g2d.drawImage(source, (height - width) / 2, (width - height) / 2, null);
                break;
                
            case 180:
                rotated = new BufferedImage(width, height, source.getType());
                g2d = rotated.createGraphics();
                g2d.rotate(Math.toRadians(180), width / 2.0, height / 2.0);
                g2d.drawImage(source, 0, 0, null);
                break;
                
            case 270:
                rotated = new BufferedImage(height, width, source.getType());
                g2d = rotated.createGraphics();
                g2d.rotate(Math.toRadians(270), height / 2.0, height / 2.0);
                g2d.drawImage(source, (height - width) / 2, (width - height) / 2, null);
                break;
                
            default:
                return source;
        }
        
        g2d.dispose();
        GameLogger.debug("Rotated texture by " + degrees + " degrees");
        return rotated;
    }
    
    /**
     * Set rendering hints based on filter mode.
     */
    private static void setRenderingHints(Graphics2D g2d, FilterMode filterMode) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        switch (filterMode) {
            case NEAREST_NEIGHBOR:
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                break;
                
            case BILINEAR:
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                break;
                
            case BICUBIC:
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                break;
                
            case AREA_AVERAGING:
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                break;
        }
    }
    
    /**
     * Get optimal filter mode for scaling operation.
     */
    public static FilterMode getOptimalFilterMode(int originalSize, int targetSize) {
        float scaleFactor = (float) targetSize / originalSize;
        
        if (scaleFactor == Math.floor(scaleFactor) && scaleFactor >= 1.0f) {
            // Integer upscaling - use nearest neighbor for pixel art
            return FilterMode.NEAREST_NEIGHBOR;
        } else if (scaleFactor < 1.0f) {
            // Downscaling - use area averaging for best quality
            return FilterMode.AREA_AVERAGING;
        } else {
            // Non-integer upscaling - use bilinear
            return FilterMode.BILINEAR;
        }
    }
}