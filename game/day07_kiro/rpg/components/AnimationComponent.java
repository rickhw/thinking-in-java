package rpg.components;

import rpg.engine.Component;
import rpg.entity.Direction;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Component that handles sprite animations with support for directional animations.
 * Manages animation states, frame timing, and directional sprite sets.
 */
public class AnimationComponent extends Component {
    private static final long serialVersionUID = 1L;
    
    // Animation data structure for directional animations
    public static class DirectionalAnimation {
        private final Map<Direction, BufferedImage[]> directionFrames;
        private final float frameDuration;
        private final boolean looping;
        
        public DirectionalAnimation(float frameDuration, boolean looping) {
            this.directionFrames = new HashMap<>();
            this.frameDuration = frameDuration;
            this.looping = looping;
        }
        
        public void setFrames(Direction direction, BufferedImage[] frames) {
            directionFrames.put(direction, frames);
        }
        
        public BufferedImage[] getFrames(Direction direction) {
            return directionFrames.get(direction);
        }
        
        public float getFrameDuration() {
            return frameDuration;
        }
        
        public boolean isLooping() {
            return looping;
        }
        
        public boolean hasDirection(Direction direction) {
            return directionFrames.containsKey(direction);
        }
    }
    
    // Animation states
    private Map<String, DirectionalAnimation> animations;
    private String currentAnimationName;
    private DirectionalAnimation currentAnimation;
    private Direction currentDirection;
    
    // Animation timing
    private float animationTimer;
    private int currentFrame;
    private boolean isPlaying;
    
    // Animation settings
    private float speedMultiplier;
    
    public AnimationComponent() {
        this.animations = new HashMap<>();
        this.currentDirection = Direction.DOWN;
        this.animationTimer = 0;
        this.currentFrame = 0;
        this.isPlaying = false;
        this.speedMultiplier = 1.0f;
    }
    
    // Animation management
    public void addAnimation(String name, DirectionalAnimation animation) {
        animations.put(name, animation);
    }
    
    public void addAnimation(String name, float frameDuration, boolean looping) {
        animations.put(name, new DirectionalAnimation(frameDuration, looping));
    }
    
    public DirectionalAnimation getAnimation(String name) {
        return animations.get(name);
    }
    
    public void setAnimationFrames(String animationName, Direction direction, BufferedImage[] frames) {
        DirectionalAnimation animation = animations.get(animationName);
        if (animation != null) {
            animation.setFrames(direction, frames);
        }
    }
    
    // Animation playback
    public void playAnimation(String name, Direction direction) {
        DirectionalAnimation animation = animations.get(name);
        if (animation != null && animation.hasDirection(direction)) {
            if (!name.equals(currentAnimationName) || direction != currentDirection) {
                currentAnimationName = name;
                currentAnimation = animation;
                currentDirection = direction;
                currentFrame = 0;
                animationTimer = 0;
                isPlaying = true;
                
                // Update render component with first frame
                updateRenderComponent();
            }
        }
    }
    
    public void stopAnimation() {
        isPlaying = false;
    }
    
    public void pauseAnimation() {
        isPlaying = false;
    }
    
    public void resumeAnimation() {
        if (currentAnimation != null) {
            isPlaying = true;
        }
    }
    
    // Animation state
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public String getCurrentAnimationName() {
        return currentAnimationName;
    }
    
    public Direction getCurrentDirection() {
        return currentDirection;
    }
    
    public int getCurrentFrame() {
        return currentFrame;
    }
    
    public void setCurrentFrame(int frame) {
        if (currentAnimation != null) {
            BufferedImage[] frames = currentAnimation.getFrames(currentDirection);
            if (frames != null && frame >= 0 && frame < frames.length) {
                currentFrame = frame;
                updateRenderComponent();
            }
        }
    }
    
    // Animation settings
    public float getSpeedMultiplier() {
        return speedMultiplier;
    }
    
    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = Math.max(0.1f, multiplier);
    }
    
    // Get current frame image
    public BufferedImage getCurrentFrameImage() {
        if (currentAnimation != null) {
            BufferedImage[] frames = currentAnimation.getFrames(currentDirection);
            if (frames != null && currentFrame < frames.length) {
                return frames[currentFrame];
            }
        }
        return null;
    }
    
    private void updateRenderComponent() {
        RenderComponent renderComponent = getEntity().getComponent(RenderComponent.class);
        if (renderComponent != null) {
            BufferedImage frameImage = getCurrentFrameImage();
            if (frameImage != null) {
                renderComponent.setSprite(frameImage);
            }
        }
    }
    
    @Override
    public void update(float deltaTime) {
        if (!isPlaying || currentAnimation == null) {
            return;
        }
        
        BufferedImage[] frames = currentAnimation.getFrames(currentDirection);
        if (frames == null || frames.length <= 1) {
            return;
        }
        
        // Update animation timer
        animationTimer += deltaTime * speedMultiplier;
        
        // Check if it's time to advance to next frame
        if (animationTimer >= currentAnimation.getFrameDuration()) {
            animationTimer = 0;
            currentFrame++;
            
            // Handle end of animation
            if (currentFrame >= frames.length) {
                if (currentAnimation.isLooping()) {
                    currentFrame = 0;
                } else {
                    currentFrame = frames.length - 1;
                    isPlaying = false;
                }
            }
            
            // Update render component with new frame
            updateRenderComponent();
        }
    }
}