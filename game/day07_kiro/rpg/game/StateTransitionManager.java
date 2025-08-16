package rpg.game;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Manages visual transitions between game states.
 * Provides fade in/out, slide, and other transition effects.
 */
public class StateTransitionManager {
    
    public enum TransitionType {
        NONE,
        FADE,
        SLIDE_LEFT,
        SLIDE_RIGHT,
        SLIDE_UP,
        SLIDE_DOWN,
        ZOOM_IN,
        ZOOM_OUT
    }
    
    private TransitionType currentTransition;
    private float transitionProgress;
    private float transitionDuration;
    private boolean transitioning;
    private Color fadeColor;
    
    public StateTransitionManager() {
        this.currentTransition = TransitionType.NONE;
        this.transitionProgress = 0.0f;
        this.transitionDuration = 0.5f; // Default 0.5 seconds
        this.transitioning = false;
        this.fadeColor = Color.BLACK;
    }
    
    /**
     * Start a transition effect.
     * @param type the type of transition
     * @param duration duration in seconds
     */
    public void startTransition(TransitionType type, float duration) {
        this.currentTransition = type;
        this.transitionDuration = duration;
        this.transitionProgress = 0.0f;
        this.transitioning = true;
    }
    
    /**
     * Start a fade transition with custom color.
     * @param duration duration in seconds
     * @param color fade color
     */
    public void startFadeTransition(float duration, Color color) {
        this.fadeColor = color;
        startTransition(TransitionType.FADE, duration);
    }
    
    /**
     * Update the transition progress.
     * @param deltaTime time elapsed since last update
     */
    public void update(float deltaTime) {
        if (!transitioning) {
            return;
        }
        
        transitionProgress += deltaTime / transitionDuration;
        
        if (transitionProgress >= 1.0f) {
            transitionProgress = 1.0f;
            transitioning = false;
        }
    }
    
    /**
     * Render the transition effect.
     * @param g2 graphics context
     */
    public void render(Graphics2D g2) {
        if (!transitioning || currentTransition == TransitionType.NONE) {
            return;
        }
        
        // Save original transform and composite
        AffineTransform originalTransform = g2.getTransform();
        Composite originalComposite = g2.getComposite();
        
        try {
            switch (currentTransition) {
                case FADE:
                    renderFadeTransition(g2);
                    break;
                case SLIDE_LEFT:
                case SLIDE_RIGHT:
                case SLIDE_UP:
                case SLIDE_DOWN:
                    renderSlideTransition(g2);
                    break;
                case ZOOM_IN:
                case ZOOM_OUT:
                    renderZoomTransition(g2);
                    break;
            }
        } finally {
            // Restore original transform and composite
            g2.setTransform(originalTransform);
            g2.setComposite(originalComposite);
        }
    }
    
    /**
     * Render fade transition effect.
     */
    private void renderFadeTransition(Graphics2D g2) {
        // Calculate alpha based on transition progress
        // Fade in: alpha goes from 1.0 to 0.0
        // Fade out: alpha goes from 0.0 to 1.0
        float alpha = Math.abs(transitionProgress - 0.5f) * 2.0f;
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        
        // Create semi-transparent overlay
        Color overlayColor = new Color(
            fadeColor.getRed(),
            fadeColor.getGreen(),
            fadeColor.getBlue(),
            (int)(alpha * 255)
        );
        
        g2.setColor(overlayColor);
        g2.fillRect(0, 0, g2.getClipBounds().width, g2.getClipBounds().height);
    }
    
    /**
     * Render slide transition effect.
     */
    private void renderSlideTransition(Graphics2D g2) {
        Rectangle bounds = g2.getClipBounds();
        if (bounds == null) return;
        
        int offsetX = 0;
        int offsetY = 0;
        
        // Calculate slide offset based on transition type and progress
        float slideProgress = transitionProgress;
        
        switch (currentTransition) {
            case SLIDE_LEFT:
                offsetX = (int)(-bounds.width * slideProgress);
                break;
            case SLIDE_RIGHT:
                offsetX = (int)(bounds.width * slideProgress);
                break;
            case SLIDE_UP:
                offsetY = (int)(-bounds.height * slideProgress);
                break;
            case SLIDE_DOWN:
                offsetY = (int)(bounds.height * slideProgress);
                break;
        }
        
        // Apply slide transform
        g2.translate(offsetX, offsetY);
    }
    
    /**
     * Render zoom transition effect.
     */
    private void renderZoomTransition(Graphics2D g2) {
        Rectangle bounds = g2.getClipBounds();
        if (bounds == null) return;
        
        float scale;
        
        if (currentTransition == TransitionType.ZOOM_IN) {
            // Zoom in: scale from 0.0 to 1.0
            scale = transitionProgress;
        } else {
            // Zoom out: scale from 1.0 to 0.0
            scale = 1.0f - transitionProgress;
        }
        
        // Ensure minimum scale to avoid division by zero
        scale = Math.max(0.1f, scale);
        
        // Center the zoom effect
        int centerX = bounds.width / 2;
        int centerY = bounds.height / 2;
        
        g2.translate(centerX, centerY);
        g2.scale(scale, scale);
        g2.translate(-centerX, -centerY);
    }
    
    /**
     * Check if a transition is currently active.
     * @return true if transitioning
     */
    public boolean isTransitioning() {
        return transitioning;
    }
    
    /**
     * Get the current transition progress (0.0 to 1.0).
     * @return transition progress
     */
    public float getTransitionProgress() {
        return transitionProgress;
    }
    
    /**
     * Get the current transition type.
     * @return transition type
     */
    public TransitionType getCurrentTransition() {
        return currentTransition;
    }
    
    /**
     * Stop the current transition immediately.
     */
    public void stopTransition() {
        this.transitioning = false;
        this.transitionProgress = 1.0f;
        this.currentTransition = TransitionType.NONE;
    }
    
    /**
     * Set the fade color for fade transitions.
     * @param color the fade color
     */
    public void setFadeColor(Color color) {
        this.fadeColor = color != null ? color : Color.BLACK;
    }
    
    /**
     * Get the current fade color.
     * @return fade color
     */
    public Color getFadeColor() {
        return fadeColor;
    }
}