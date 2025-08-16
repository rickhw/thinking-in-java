package rpg.assets;

import rpg.utils.GameLogger;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sprite animation data structures and management.
 * Handles animation sequences, timing, and playback control.
 */
public class SpriteAnimationData {
    private static final GameLogger logger = GameLogger.getInstance();
    
    private final String name;
    private final Map<String, AnimationSequence> sequences = new HashMap<>();
    private String currentSequence = null;
    private AnimationState state = AnimationState.STOPPED;
    
    public SpriteAnimationData(String name) {
        this.name = name;
    }
    
    /**
     * Add an animation sequence.
     */
    public void addSequence(String sequenceName, AnimationSequence sequence) {
        sequences.put(sequenceName, sequence);
        
        // Set as current if it's the first sequence
        if (currentSequence == null) {
            currentSequence = sequenceName;
        }
        
        logger.debug("Added animation sequence: " + sequenceName + " with " + 
                    sequence.getFrameCount() + " frames");
    }
    
    /**
     * Create and add a simple animation sequence.
     */
    public void addSequence(String sequenceName, BufferedImage[] frames, float frameDuration) {
        List<AnimationFrame> animFrames = new ArrayList<>();
        for (BufferedImage frame : frames) {
            animFrames.add(new AnimationFrame(frame, frameDuration));
        }
        
        AnimationSequence sequence = new AnimationSequence(sequenceName, animFrames);
        addSequence(sequenceName, sequence);
    }
    
    /**
     * Create and add an animation sequence with individual frame durations.
     */
    public void addSequence(String sequenceName, BufferedImage[] frames, float[] frameDurations) {
        if (frames.length != frameDurations.length) {
            throw new IllegalArgumentException("Frame count must match duration count");
        }
        
        List<AnimationFrame> animFrames = new ArrayList<>();
        for (int i = 0; i < frames.length; i++) {
            animFrames.add(new AnimationFrame(frames[i], frameDurations[i]));
        }
        
        AnimationSequence sequence = new AnimationSequence(sequenceName, animFrames);
        addSequence(sequenceName, sequence);
    }
    
    /**
     * Set the current animation sequence.
     */
    public void setCurrentSequence(String sequenceName) {
        if (!sequences.containsKey(sequenceName)) {
            logger.warn("Animation sequence not found: " + sequenceName);
            return;
        }
        
        if (!sequenceName.equals(currentSequence)) {
            currentSequence = sequenceName;
            getCurrentSequence().reset();
            logger.debug("Changed animation sequence to: " + sequenceName);
        }
    }
    
    /**
     * Get the current animation sequence.
     */
    public AnimationSequence getCurrentSequence() {
        return sequences.get(currentSequence);
    }
    
    /**
     * Update animation timing.
     */
    public void update(float deltaTime) {
        if (state == AnimationState.PLAYING && currentSequence != null) {
            AnimationSequence sequence = getCurrentSequence();
            if (sequence != null) {
                sequence.update(deltaTime);
            }
        }
    }
    
    /**
     * Get current frame image.
     */
    public BufferedImage getCurrentFrame() {
        AnimationSequence sequence = getCurrentSequence();
        return sequence != null ? sequence.getCurrentFrame() : null;
    }
    
    /**
     * Play the current animation.
     */
    public void play() {
        state = AnimationState.PLAYING;
        AnimationSequence sequence = getCurrentSequence();
        if (sequence != null) {
            sequence.play();
        }
    }
    
    /**
     * Play a specific animation sequence.
     */
    public void play(String sequenceName) {
        setCurrentSequence(sequenceName);
        play();
    }
    
    /**
     * Pause the animation.
     */
    public void pause() {
        state = AnimationState.PAUSED;
        AnimationSequence sequence = getCurrentSequence();
        if (sequence != null) {
            sequence.pause();
        }
    }
    
    /**
     * Stop the animation.
     */
    public void stop() {
        state = AnimationState.STOPPED;
        AnimationSequence sequence = getCurrentSequence();
        if (sequence != null) {
            sequence.stop();
        }
    }
    
    /**
     * Reset the current animation to the first frame.
     */
    public void reset() {
        AnimationSequence sequence = getCurrentSequence();
        if (sequence != null) {
            sequence.reset();
        }
    }
    
    /**
     * Check if animation is playing.
     */
    public boolean isPlaying() {
        return state == AnimationState.PLAYING;
    }
    
    /**
     * Check if animation is finished (for non-looping animations).
     */
    public boolean isFinished() {
        AnimationSequence sequence = getCurrentSequence();
        return sequence != null && sequence.isFinished();
    }
    
    /**
     * Get animation name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get current sequence name.
     */
    public String getCurrentSequenceName() {
        return currentSequence;
    }
    
    /**
     * Get all sequence names.
     */
    public String[] getSequenceNames() {
        return sequences.keySet().toArray(new String[0]);
    }
    
    /**
     * Check if a sequence exists.
     */
    public boolean hasSequence(String sequenceName) {
        return sequences.containsKey(sequenceName);
    }
    
    /**
     * Get animation state.
     */
    public AnimationState getState() {
        return state;
    }
    
    /**
     * Animation sequence class.
     */
    public static class AnimationSequence {
        private final String name;
        private final List<AnimationFrame> frames;
        private float currentTime = 0f;
        private int currentFrameIndex = 0;
        private boolean looping = true;
        private boolean playing = false;
        private PlaybackMode playbackMode = PlaybackMode.FORWARD;
        private int playbackDirection = 1; // 1 for forward, -1 for backward
        
        public AnimationSequence(String name, List<AnimationFrame> frames) {
            this.name = name;
            this.frames = new ArrayList<>(frames);
        }
        
        /**
         * Update animation timing.
         */
        public void update(float deltaTime) {
            if (!playing || frames.isEmpty()) {
                return;
            }
            
            currentTime += deltaTime;
            AnimationFrame currentFrame = frames.get(currentFrameIndex);
            
            if (currentTime >= currentFrame.duration) {
                currentTime = 0f;
                advanceFrame();
            }
        }
        
        /**
         * Advance to the next frame based on playback mode.
         */
        private void advanceFrame() {
            switch (playbackMode) {
                case FORWARD:
                    currentFrameIndex++;
                    if (currentFrameIndex >= frames.size()) {
                        if (looping) {
                            currentFrameIndex = 0;
                        } else {
                            currentFrameIndex = frames.size() - 1;
                            playing = false;
                        }
                    }
                    break;
                    
                case BACKWARD:
                    currentFrameIndex--;
                    if (currentFrameIndex < 0) {
                        if (looping) {
                            currentFrameIndex = frames.size() - 1;
                        } else {
                            currentFrameIndex = 0;
                            playing = false;
                        }
                    }
                    break;
                    
                case PING_PONG:
                    currentFrameIndex += playbackDirection;
                    if (currentFrameIndex >= frames.size() - 1) {
                        playbackDirection = -1;
                        currentFrameIndex = frames.size() - 1;
                    } else if (currentFrameIndex <= 0) {
                        playbackDirection = 1;
                        currentFrameIndex = 0;
                        if (!looping) {
                            playing = false;
                        }
                    }
                    break;
            }
        }
        
        /**
         * Get current frame image.
         */
        public BufferedImage getCurrentFrame() {
            if (frames.isEmpty()) {
                return null;
            }
            return frames.get(currentFrameIndex).image;
        }
        
        /**
         * Start playing the animation.
         */
        public void play() {
            playing = true;
        }
        
        /**
         * Stop the animation.
         */
        public void stop() {
            playing = false;
            currentFrameIndex = 0;
            currentTime = 0f;
            playbackDirection = 1;
        }
        
        /**
         * Pause the animation.
         */
        public void pause() {
            playing = false;
        }
        
        /**
         * Reset animation to first frame.
         */
        public void reset() {
            currentFrameIndex = 0;
            currentTime = 0f;
            playbackDirection = 1;
        }
        
        /**
         * Check if animation is finished.
         */
        public boolean isFinished() {
            return !looping && !playing && 
                   ((playbackMode == PlaybackMode.FORWARD && currentFrameIndex >= frames.size() - 1) ||
                    (playbackMode == PlaybackMode.BACKWARD && currentFrameIndex <= 0));
        }
        
        // Getters and setters
        public String getName() { return name; }
        public boolean isLooping() { return looping; }
        public void setLooping(boolean looping) { this.looping = looping; }
        public boolean isPlaying() { return playing; }
        public int getFrameCount() { return frames.size(); }
        public int getCurrentFrameIndex() { return currentFrameIndex; }
        public PlaybackMode getPlaybackMode() { return playbackMode; }
        public void setPlaybackMode(PlaybackMode playbackMode) { this.playbackMode = playbackMode; }
        
        public void setCurrentFrame(int frameIndex) {
            if (frameIndex >= 0 && frameIndex < frames.size()) {
                this.currentFrameIndex = frameIndex;
                this.currentTime = 0f;
            }
        }
        
        public float getTotalDuration() {
            float total = 0f;
            for (AnimationFrame frame : frames) {
                total += frame.duration;
            }
            return total;
        }
    }
    
    /**
     * Individual animation frame.
     */
    public static class AnimationFrame {
        public final BufferedImage image;
        public final float duration;
        public final Map<String, Object> metadata = new HashMap<>();
        
        public AnimationFrame(BufferedImage image, float duration) {
            this.image = image;
            this.duration = duration;
        }
        
        public void setMetadata(String key, Object value) {
            metadata.put(key, value);
        }
        
        public Object getMetadata(String key) {
            return metadata.get(key);
        }
    }
    
    /**
     * Animation playback modes.
     */
    public enum PlaybackMode {
        FORWARD,    // Play frames 0 -> N
        BACKWARD,   // Play frames N -> 0
        PING_PONG   // Play frames 0 -> N -> 0
    }
    
    /**
     * Animation states.
     */
    public enum AnimationState {
        STOPPED,
        PLAYING,
        PAUSED
    }
}