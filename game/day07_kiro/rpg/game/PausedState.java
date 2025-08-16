package rpg.game;

import rpg.systems.EventBus;
import rpg.utils.GameLogger;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Paused state that overlays the playing state and pauses game logic.
 */
public class PausedState extends AbstractGameState {
    
    private Font pauseFont;
    private String pauseText;
    private Color overlayColor;
    private Color textColor;
    
    public PausedState(EventBus eventBus) {
        super("PAUSED", eventBus);
    }
    
    @Override
    protected void initialize() {
        GameLogger.info("Initializing paused state");
        
        // Initialize pause screen visuals
        this.pauseFont = new Font("Arial", Font.BOLD, 48);
        this.pauseText = "PAUSED";
        this.overlayColor = new Color(0, 0, 0, 128); // Semi-transparent black
        this.textColor = Color.WHITE;
    }
    
    @Override
    protected void onEnter() {
        GameLogger.info("Game paused");
        // Game logic will be paused automatically by the StateManager
        // since this state returns true for pausesUnderlyingStates()
    }
    
    @Override
    protected void onExit() {
        GameLogger.info("Game unpaused");
        // Game logic will resume automatically
    }
    
    @Override
    protected void onUpdate(float deltaTime) {
        // Paused state doesn't update game logic
        // Could add pause menu animations here if desired
    }
    
    @Override
    protected void onRender(Graphics2D g2) {
        // Get screen dimensions
        Rectangle bounds = g2.getClipBounds();
        if (bounds == null) {
            bounds = new Rectangle(0, 0, 800, 600); // Default size
        }
        
        // Draw semi-transparent overlay
        g2.setColor(overlayColor);
        g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        
        // Draw pause text
        g2.setColor(textColor);
        g2.setFont(pauseFont);
        
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(pauseText);
        int textHeight = fm.getHeight();
        
        int x = bounds.x + (bounds.width - textWidth) / 2;
        int y = bounds.y + (bounds.height - textHeight) / 2 + fm.getAscent();
        
        g2.drawString(pauseText, x, y);
        
        // Draw instructions
        String instruction = "Press ESC to resume";
        Font instructionFont = new Font("Arial", Font.PLAIN, 16);
        g2.setFont(instructionFont);
        
        FontMetrics instructionFm = g2.getFontMetrics();
        int instructionWidth = instructionFm.stringWidth(instruction);
        int instructionX = bounds.x + (bounds.width - instructionWidth) / 2;
        int instructionY = y + 60;
        
        g2.drawString(instruction, instructionX, instructionY);
    }
    
    @Override
    protected void onHandleInput(InputEvent inputEvent) {
        if (inputEvent.getType() == InputEvent.Type.KEY_PRESSED) {
            int keyCode = inputEvent.getKeyCode();
            
            // Handle unpause key (ESC)
            if (keyCode == KeyEvent.VK_ESCAPE) {
                // Request to pop this state (unpause)
                GameLogger.info("Unpause requested");
                // This would typically trigger the StateManager to pop this state
            }
        }
    }
    
    @Override
    public boolean pausesUnderlyingStates() {
        return true; // Pause the game logic underneath
    }
    
    @Override
    public boolean rendersOverUnderlyingStates() {
        return true; // Render over the game world
    }
}