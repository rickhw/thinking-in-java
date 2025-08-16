package rpg.assets;

import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sprite sheet for loading and managing sprite animations.
 * Supports frame extraction, animation sequences, and sprite metadata.
 */
public class SpriteSheet {
    private static final GameLogger logger = GameLogger.getInstance();
    
    private final String name;
    private final BufferedImage sheetImage;
    private final int spriteWidth;
    private final int spriteHeight;
    private final int columns;
    private final int rows;
    private final Map<String, Animation> animations = new HashMap<>();
    private final List<BufferedImage> frames = new ArrayList<>();
    
    /**
     * Create a sprite sheet from an image.
     */
    public SpriteSheet(String name, BufferedImage sheetImage, int spriteWidth, int spriteHeight) {
        this.name = name;
        this.sheetImage = sheetImage;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.columns = sheetImage.getWidth() / spriteWidth;
        this.rows = sheetImage.getHeight() / spriteHeight;
        
        extractFrames();
    }
    
    /**
     * Extract all frames from the sprite sheet.
     */
    private void extractFrames() {
        frames.clear();
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int x = col * spriteWidth;
                int y = row * spriteHeight;
                
                BufferedImage frame = sheetImage.getSubimage(x, y, spriteWidth, spriteHeight);
                frames.add(frame);
            }
        }
        
        logger.info("Extracted " + frames.size() + " frames from sprite sheet: " + name);
    }
    
    /**
     * Get a specific frame by index.
     */
    public BufferedImage getFrame(int frameIndex) {
        if (frameIndex >= 0 && frameIndex < frames.size()) {
            return frames.get(frameIndex);
        }
        
        logger.warn("Invalid frame index: " + frameIndex + " (max: " + (frames.size() - 1) + ")");
        return null;
    }
    
    /**
     * Get a frame by row and column.
     */
    public BufferedImage getFrame(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < columns) {
            int frameIndex = row * columns + col;
            return frames.get(frameIndex);
        }
        
        logger.warn("Invalid frame coordinates: (" + row + ", " + col + ")");
        return null;
    }
    
    /**
     * Get frames in a row as an animation sequence.
     */
    public BufferedImage[] getRowFrames(int row) {
        if (row < 0 || row >= rows) {
            logger.warn("Invalid row: " + row);
            return new BufferedImage[0];
        }
        
        BufferedImage[] rowFrames = new BufferedImage[columns];
        for (int col = 0; col < columns; col++) {
            rowFrames[col] = getFrame(row, col);
        }
        
        return rowFrames;
    }
    
    /**
     * Get frames in a column as an animation sequence.
     */
    public BufferedImage[] getColumnFrames(int col) {
        if (col < 0 || col >= columns) {
            logger.warn("Invalid column: " + col);
            return new BufferedImage[0];
        }
        
        BufferedImage[] colFrames = new BufferedImage[rows];
        for (int row = 0; row < rows; row++) {
            colFrames[row] = getFrame(row, col);
        }
        
        return colFrames;
    }
    
    /**
     * Create an animation from a range of frames.
     */
    public Animation createAnimation(String animationName, int startFrame, int endFrame, float frameDuration) {
        if (startFrame < 0 || endFrame >= frames.size() || startFrame > endFrame) {
            throw new IllegalArgumentException("Invalid frame range: " + startFrame + " to " + endFrame);
        }
        
        List<AnimationFrame> animFrames = new ArrayList<>();
        for (int i = startFrame; i <= endFrame; i++) {
            animFrames.add(new AnimationFrame(frames.get(i), frameDuration));
        }
        
        Animation animation = new Animation(animationName, animFrames);
        animations.put(animationName, animation);
        
        logger.debug("Created animation: " + animationName + " with " + animFrames.size() + " frames");
        return animation;
    }
    
    /**
     * Create an animation from a row of frames.
     */
    public Animation createRowAnimation(String animationName, int row, float frameDuration) {
        BufferedImage[] rowFrames = getRowFrames(row);
        
        List<AnimationFrame> animFrames = new ArrayList<>();
        for (BufferedImage frame : rowFrames) {
            animFrames.add(new AnimationFrame(frame, frameDuration));
        }
        
        Animation animation = new Animation(animationName, animFrames);
        animations.put(animationName, animation);
        
        return animation;
    }
    
    /**
     * Get an animation by name.
     */
    public Animation getAnimation(String animationName) {
        return animations.get(animationName);
    }
    
    /**
     * Check if an animation exists.
     */
    public boolean hasAnimation(String animationName) {
        return animations.containsKey(animationName);
    }
    
    /**
     * Get all animation names.
     */
    public String[] getAnimationNames() {
        return animations.keySet().toArray(new String[0]);
    }
    
    /**
     * Get sprite sheet name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get sprite dimensions.
     */
    public int getSpriteWidth() {
        return spriteWidth;
    }
    
    public int getSpriteHeight() {
        return spriteHeight;
    }
    
    /**
     * Get sheet dimensions.
     */
    public int getColumns() {
        return columns;
    }
    
    public int getRows() {
        return rows;
    }
    
    /**
     * Get total frame count.
     */
    public int getFrameCount() {
        return frames.size();
    }
    
    /**
     * Get the original sheet image.
     */
    public BufferedImage getSheetImage() {
        return sheetImage;
    }
    
    /**
     * Animation class for sprite animations.
     */
    public static class Animation {
        private final String name;
        private final List<AnimationFrame> frames;
        private float currentTime = 0f;
        private int currentFrameIndex = 0;
        private boolean looping = true;
        private boolean playing = false;
        
        public Animation(String name, List<AnimationFrame> frames) {
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
                currentFrameIndex++;
                
                if (currentFrameIndex >= frames.size()) {
                    if (looping) {
                        currentFrameIndex = 0;
                    } else {
                        currentFrameIndex = frames.size() - 1;
                        playing = false;
                    }
                }
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
        }
        
        /**
         * Check if animation is finished (for non-looping animations).
         */
        public boolean isFinished() {
            return !looping && currentFrameIndex >= frames.size() - 1 && !playing;
        }
        
        // Getters and setters
        public String getName() { return name; }
        public boolean isLooping() { return looping; }
        public void setLooping(boolean looping) { this.looping = looping; }
        public boolean isPlaying() { return playing; }
        public int getFrameCount() { return frames.size(); }
        public int getCurrentFrameIndex() { return currentFrameIndex; }
        
        public void setCurrentFrame(int frameIndex) {
            if (frameIndex >= 0 && frameIndex < frames.size()) {
                this.currentFrameIndex = frameIndex;
                this.currentTime = 0f;
            }
        }
    }
    
    /**
     * Individual animation frame.
     */
    public static class AnimationFrame {
        public final BufferedImage image;
        public final float duration;
        
        public AnimationFrame(BufferedImage image, float duration) {
            this.image = image;
            this.duration = duration;
        }
    }
}