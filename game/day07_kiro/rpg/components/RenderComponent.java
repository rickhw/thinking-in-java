package rpg.components;

import rpg.engine.Component;
import java.awt.image.BufferedImage;

/**
 * Component that holds rendering information for an entity.
 * Includes sprite data, animation state, and rendering layer.
 */
public class RenderComponent extends Component {
    private BufferedImage sprite;
    private int layer;
    private boolean visible;
    private float alpha;
    private boolean flipX;
    private boolean flipY;
    
    // Animation properties
    private BufferedImage[] animationFrames;
    private int currentFrame;
    private float animationTimer;
    private float frameDuration;
    private boolean isAnimating;
    private boolean loopAnimation;
    
    public RenderComponent() {
        this.layer = 0;
        this.visible = true;
        this.alpha = 1.0f;
        this.flipX = false;
        this.flipY = false;
        this.currentFrame = 0;
        this.animationTimer = 0;
        this.frameDuration = 0.1f; // Default 100ms per frame
        this.isAnimating = false;
        this.loopAnimation = true;
    }
    
    public RenderComponent(BufferedImage sprite) {
        this();
        this.sprite = sprite;
    }
    
    public RenderComponent(BufferedImage sprite, int layer) {
        this(sprite);
        this.layer = layer;
    }
    
    // Sprite properties
    public BufferedImage getSprite() {
        return sprite;
    }
    
    public void setSprite(BufferedImage sprite) {
        this.sprite = sprite;
    }
    
    public int getLayer() {
        return layer;
    }
    
    public void setLayer(int layer) {
        this.layer = layer;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public float getAlpha() {
        return alpha;
    }
    
    public void setAlpha(float alpha) {
        this.alpha = Math.max(0.0f, Math.min(1.0f, alpha));
    }
    
    public boolean isFlipX() {
        return flipX;
    }
    
    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }
    
    public boolean isFlipY() {
        return flipY;
    }
    
    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }
    
    // Animation methods
    public void setAnimation(BufferedImage[] frames, float frameDuration, boolean loop) {
        this.animationFrames = frames;
        this.frameDuration = frameDuration;
        this.loopAnimation = loop;
        this.currentFrame = 0;
        this.animationTimer = 0;
        this.isAnimating = frames != null && frames.length > 0;
        
        if (isAnimating && frames.length > 0) {
            this.sprite = frames[0];
        }
    }
    
    public void startAnimation() {
        if (animationFrames != null && animationFrames.length > 0) {
            isAnimating = true;
            currentFrame = 0;
            animationTimer = 0;
            sprite = animationFrames[0];
        }
    }
    
    public void stopAnimation() {
        isAnimating = false;
    }
    
    public boolean isAnimating() {
        return isAnimating;
    }
    
    public int getCurrentFrame() {
        return currentFrame;
    }
    
    public void setCurrentFrame(int frame) {
        if (animationFrames != null && frame >= 0 && frame < animationFrames.length) {
            currentFrame = frame;
            sprite = animationFrames[frame];
        }
    }
    
    @Override
    public void update(float deltaTime) {
        if (isAnimating && animationFrames != null && animationFrames.length > 1) {
            animationTimer += deltaTime;
            
            if (animationTimer >= frameDuration) {
                animationTimer = 0;
                currentFrame++;
                
                if (currentFrame >= animationFrames.length) {
                    if (loopAnimation) {
                        currentFrame = 0;
                    } else {
                        currentFrame = animationFrames.length - 1;
                        isAnimating = false;
                    }
                }
                
                sprite = animationFrames[currentFrame];
            }
        }
    }
}