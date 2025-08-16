package rpg.game;

import rpg.systems.EventBus;
import rpg.utils.GameLogger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Loading state displayed while assets and resources are being loaded.
 */
public class LoadingState extends AbstractGameState {
    
    private Font titleFont;
    private Font statusFont;
    private Color backgroundColor;
    private Color textColor;
    private Color progressColor;
    private String title;
    private String currentStatus;
    private float progress;
    private List<String> loadingMessages;
    private int currentMessageIndex;
    private float messageTimer;
    private float messageInterval;
    
    // Loading animation
    private float animationTimer;
    private String[] animationFrames;
    private int currentFrame;
    
    public LoadingState(EventBus eventBus) {
        super("LOADING", eventBus);
    }
    
    @Override
    protected void initialize() {
        GameLogger.info("Initializing loading state");
        
        // Initialize visuals
        this.titleFont = new Font("Arial", Font.BOLD, 32);
        this.statusFont = new Font("Arial", Font.PLAIN, 18);
        this.backgroundColor = new Color(10, 10, 20);
        this.textColor = Color.WHITE;
        this.progressColor = new Color(100, 150, 255);
        this.title = "Loading...";
        this.currentStatus = "Initializing";
        this.progress = 0.0f;
        this.messageTimer = 0.0f;
        this.messageInterval = 2.0f; // Change message every 2 seconds
        this.currentMessageIndex = 0;
        
        // Initialize loading messages
        this.loadingMessages = new ArrayList<>();
        loadingMessages.add("Loading game assets...");
        loadingMessages.add("Preparing world data...");
        loadingMessages.add("Initializing systems...");
        loadingMessages.add("Setting up graphics...");
        loadingMessages.add("Loading audio files...");
        loadingMessages.add("Finalizing setup...");
        
        // Initialize animation
        this.animationTimer = 0.0f;
        this.animationFrames = new String[]{".", "..", "...", "...."};
        this.currentFrame = 0;
    }
    
    @Override
    protected void onEnter() {
        GameLogger.info("Entered loading state");
        resetProgress();
    }
    
    @Override
    protected void onExit() {
        GameLogger.info("Exited loading state");
    }
    
    @Override
    protected void onUpdate(float deltaTime) {
        // Update animation
        animationTimer += deltaTime;
        if (animationTimer >= 0.5f) { // Change frame every 0.5 seconds
            animationTimer = 0.0f;
            currentFrame = (currentFrame + 1) % animationFrames.length;
        }
        
        // Update loading messages
        messageTimer += deltaTime;
        if (messageTimer >= messageInterval) {
            messageTimer = 0.0f;
            currentMessageIndex = (currentMessageIndex + 1) % loadingMessages.size();
            currentStatus = loadingMessages.get(currentMessageIndex);
        }
        
        // Simulate loading progress (in a real implementation, this would be driven by actual loading)
        if (progress < 1.0f) {
            progress += deltaTime * 0.2f; // 20% per second
            progress = Math.min(1.0f, progress);
        }
    }
    
    @Override
    protected void onRender(Graphics2D g2) {
        // Get screen dimensions
        Rectangle bounds = g2.getClipBounds();
        if (bounds == null) {
            bounds = new Rectangle(0, 0, 800, 600); // Default size
        }
        
        // Draw background
        g2.setColor(backgroundColor);
        g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        
        // Draw title with animation
        g2.setColor(textColor);
        g2.setFont(titleFont);
        
        String animatedTitle = title + animationFrames[currentFrame];
        FontMetrics titleFm = g2.getFontMetrics();
        int titleWidth = titleFm.stringWidth(animatedTitle);
        int titleX = bounds.x + (bounds.width - titleWidth) / 2;
        int titleY = bounds.y + bounds.height / 2 - 50;
        
        g2.drawString(animatedTitle, titleX, titleY);
        
        // Draw progress bar
        int progressBarWidth = 400;
        int progressBarHeight = 20;
        int progressBarX = bounds.x + (bounds.width - progressBarWidth) / 2;
        int progressBarY = titleY + 50;
        
        // Draw progress bar background
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(progressBarX, progressBarY, progressBarWidth, progressBarHeight);
        
        // Draw progress bar fill
        g2.setColor(progressColor);
        int fillWidth = (int)(progressBarWidth * progress);
        g2.fillRect(progressBarX, progressBarY, fillWidth, progressBarHeight);
        
        // Draw progress bar border
        g2.setColor(textColor);
        g2.drawRect(progressBarX, progressBarY, progressBarWidth, progressBarHeight);
        
        // Draw progress percentage
        String progressText = String.format("%.0f%%", progress * 100);
        FontMetrics progressFm = g2.getFontMetrics(statusFont);
        int progressTextWidth = progressFm.stringWidth(progressText);
        int progressTextX = progressBarX + (progressBarWidth - progressTextWidth) / 2;
        int progressTextY = progressBarY + progressBarHeight + 25;
        
        g2.setFont(statusFont);
        g2.drawString(progressText, progressTextX, progressTextY);
        
        // Draw current status
        g2.setColor(textColor);
        FontMetrics statusFm = g2.getFontMetrics();
        int statusWidth = statusFm.stringWidth(currentStatus);
        int statusX = bounds.x + (bounds.width - statusWidth) / 2;
        int statusY = progressTextY + 40;
        
        g2.drawString(currentStatus, statusX, statusY);
    }
    
    @Override
    protected void onHandleInput(InputEvent inputEvent) {
        // Loading state typically doesn't handle input
        // Could add a "skip" option if desired
    }
    
    /**
     * Set the loading progress.
     * @param progress progress value between 0.0 and 1.0
     */
    public void setProgress(float progress) {
        this.progress = Math.max(0.0f, Math.min(1.0f, progress));
    }
    
    /**
     * Get the current loading progress.
     * @return progress value between 0.0 and 1.0
     */
    public float getProgress() {
        return progress;
    }
    
    /**
     * Set the current loading status message.
     * @param status the status message
     */
    public void setStatus(String status) {
        this.currentStatus = status != null ? status : "Loading...";
    }
    
    /**
     * Get the current loading status.
     * @return current status message
     */
    public String getStatus() {
        return currentStatus;
    }
    
    /**
     * Reset the loading progress to 0.
     */
    public void resetProgress() {
        this.progress = 0.0f;
        this.currentStatus = "Initializing";
        this.messageTimer = 0.0f;
        this.currentMessageIndex = 0;
    }
    
    /**
     * Check if loading is complete.
     * @return true if progress is 100%
     */
    public boolean isComplete() {
        return progress >= 1.0f;
    }
    
    @Override
    public boolean pausesUnderlyingStates() {
        return true; // Loading pauses everything underneath
    }
    
    @Override
    public boolean rendersOverUnderlyingStates() {
        return false; // Loading renders its own full screen
    }
}