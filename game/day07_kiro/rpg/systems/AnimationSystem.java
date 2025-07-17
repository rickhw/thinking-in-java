package rpg.systems;

import rpg.components.RenderComponent;
import rpg.components.MovementComponent;
import rpg.components.InputComponent;
import rpg.engine.Entity;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.awt.image.BufferedImage;

/**
 * System responsible for updating sprite animations based on entity state.
 * Handles animation transitions, frame updates, and state-based animation selection.
 */
public class AnimationSystem extends GameSystem {
    private Map<Integer, AnimationState> entityAnimationStates;
    
    // Animation settings
    private float defaultFrameDuration = 0.15f; // 150ms per frame
    
    @Override
    public void initialize() {
        this.entityAnimationStates = new HashMap<>();
    }
    
    /**
     * Set the default frame duration for animations
     */
    public void setDefaultFrameDuration(float duration) {
        this.defaultFrameDuration = duration;
    }
    
    @Override
    public void update(float deltaTime) {
        if (!isEnabled()) return;
        
        // Get all entities with render components (potential animations)
        List<Entity> renderableEntities = entityManager.getEntitiesWith(RenderComponent.class);
        
        for (Entity entity : renderableEntities) {
            updateEntityAnimation(entity, deltaTime);
        }
    }
    
    private void updateEntityAnimation(Entity entity, float deltaTime) {
        RenderComponent render = entity.getComponent(RenderComponent.class);
        if (render == null) return;
        
        // Update the render component's animation
        render.update(deltaTime);
        
        // Determine what animation should be playing based on entity state
        String desiredAnimation = determineAnimation(entity);
        
        // Get or create animation state for this entity
        AnimationState animState = entityAnimationStates.computeIfAbsent(
            entity.getId(), k -> new AnimationState());
        
        // Check if we need to change animation
        if (!desiredAnimation.equals(animState.currentAnimation)) {
            changeAnimation(entity, render, desiredAnimation, animState);
        }
        
        // Update animation state
        animState.currentAnimation = desiredAnimation;
    }
    
    private String determineAnimation(Entity entity) {
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        InputComponent input = entity.getComponent(InputComponent.class);
        
        // Default animation
        String animation = "idle";
        
        // Check movement state
        if (movement != null && (Math.abs(movement.velocityX) > 0.1f || Math.abs(movement.velocityY) > 0.1f)) {
            // Determine direction based on velocity or input
            if (input != null) {
                if (input.isMovingUp()) {
                    animation = input.isRunning() ? "run_up" : "walk_up";
                } else if (input.isMovingDown()) {
                    animation = input.isRunning() ? "run_down" : "walk_down";
                } else if (input.isMovingLeft()) {
                    animation = input.isRunning() ? "run_left" : "walk_left";
                } else if (input.isMovingRight()) {
                    animation = input.isRunning() ? "run_right" : "walk_right";
                }
            } else {
                // Determine direction based on velocity
                if (Math.abs(movement.velocityX) > Math.abs(movement.velocityY)) {
                    animation = movement.velocityX > 0 ? "walk_right" : "walk_left";
                } else {
                    animation = movement.velocityY > 0 ? "walk_down" : "walk_up";
                }
            }
        }
        
        // Check for other states (attack, interact, etc.)
        if (input != null) {
            if (input.isActionJustPressed("attack")) {
                animation = "attack";
            } else if (input.isActionJustPressed("interact")) {
                animation = "interact";
            }
        }
        
        return animation;
    }
    
    private void changeAnimation(Entity entity, RenderComponent render, 
                               String animationName, AnimationState animState) {
        
        // This is a simplified animation system
        // In a full implementation, you would load animation data from files
        // For now, we'll work with the existing RenderComponent animation system
        
        // Check if the render component has animation frames
        if (render.isAnimating()) {
            // Animation is already set up, just ensure it's playing
            if (!animationName.equals("idle")) {
                render.startAnimation();
            } else {
                render.stopAnimation();
            }
        }
        
        // Handle direction-based sprite flipping for simple animations
        handleDirectionFlipping(render, animationName);
        
        animState.previousAnimation = animState.currentAnimation;
        animState.animationStartTime = System.currentTimeMillis();
    }
    
    private void handleDirectionFlipping(RenderComponent render, String animationName) {
        // Simple sprite flipping based on animation direction
        if (animationName.contains("_left")) {
            render.setFlipX(true);
        } else if (animationName.contains("_right")) {
            render.setFlipX(false);
        }
        // Up and down animations don't need flipping
    }
    
    /**
     * Set up an animation for an entity
     */
    public void setupAnimation(Entity entity, String animationName, 
                             BufferedImage[] frames, float frameDuration, boolean loop) {
        RenderComponent render = entity.getComponent(RenderComponent.class);
        if (render != null) {
            render.setAnimation(frames, frameDuration, loop);
            
            AnimationState animState = entityAnimationStates.computeIfAbsent(
                entity.getId(), k -> new AnimationState());
            animState.availableAnimations.put(animationName, new AnimationData(frames, frameDuration, loop));
        }
    }
    
    /**
     * Play a specific animation on an entity
     */
    public void playAnimation(Entity entity, String animationName) {
        RenderComponent render = entity.getComponent(RenderComponent.class);
        AnimationState animState = entityAnimationStates.get(entity.getId());
        
        if (render != null && animState != null) {
            AnimationData animData = animState.availableAnimations.get(animationName);
            if (animData != null) {
                render.setAnimation(animData.frames, animData.frameDuration, animData.loop);
                render.startAnimation();
                animState.currentAnimation = animationName;
            }
        }
    }
    
    /**
     * Stop animation on an entity
     */
    public void stopAnimation(Entity entity) {
        RenderComponent render = entity.getComponent(RenderComponent.class);
        if (render != null) {
            render.stopAnimation();
        }
        
        AnimationState animState = entityAnimationStates.get(entity.getId());
        if (animState != null) {
            animState.currentAnimation = "idle";
        }
    }
    
    /**
     * Check if an entity is currently playing a specific animation
     */
    public boolean isPlayingAnimation(Entity entity, String animationName) {
        AnimationState animState = entityAnimationStates.get(entity.getId());
        return animState != null && animationName.equals(animState.currentAnimation);
    }
    
    @Override
    public void cleanup() {
        if (entityAnimationStates != null) {
            entityAnimationStates.clear();
        }
    }
    
    @Override
    public int getPriority() {
        return 150; // Animation should happen after input but before rendering
    }
    
    /**
     * Helper class to track animation state for entities
     */
    private static class AnimationState {
        String currentAnimation = "idle";
        String previousAnimation = "idle";
        long animationStartTime = 0;
        Map<String, AnimationData> availableAnimations = new HashMap<>();
    }
    
    /**
     * Helper class to store animation data
     */
    private static class AnimationData {
        final BufferedImage[] frames;
        final float frameDuration;
        final boolean loop;
        
        AnimationData(BufferedImage[] frames, float frameDuration, boolean loop) {
            this.frames = frames;
            this.frameDuration = frameDuration;
            this.loop = loop;
        }
    }
}