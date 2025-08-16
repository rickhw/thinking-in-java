package rpg.game;

import rpg.GamePanel;
import rpg.systems.EventBus;
import rpg.utils.GameLogger;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * Main gameplay state where the player controls their character and interacts with the world.
 */
public class PlayingState extends AbstractGameState {
    
    private final GamePanel gamePanel;
    
    public PlayingState(EventBus eventBus, GamePanel gamePanel) {
        super("PLAYING", eventBus);
        this.gamePanel = gamePanel;
    }
    
    @Override
    protected void initialize() {
        GameLogger.info("Initializing playing state");
        // Initialize gameplay systems if needed
    }
    
    @Override
    protected void onEnter() {
        GameLogger.info("Entering playing state - game is now active");
        // Resume game systems if they were paused
    }
    
    @Override
    protected void onExit() {
        GameLogger.info("Exiting playing state");
        // Pause or cleanup game systems if needed
    }
    
    @Override
    protected void onUpdate(float deltaTime) {
        // Update game logic
        if (gamePanel != null) {
            gamePanel.update();
        }
    }
    
    @Override
    protected void onRender(Graphics2D g2) {
        // Render game world
        if (gamePanel != null) {
            // The GamePanel's paintComponent will handle the actual rendering
            // This state just ensures the game world is being rendered
        }
    }
    
    @Override
    protected void onHandleInput(InputEvent inputEvent) {
        // Handle gameplay input
        if (inputEvent.getType() == InputEvent.Type.KEY_PRESSED) {
            int keyCode = inputEvent.getKeyCode();
            
            // Handle pause key (ESC)
            if (keyCode == KeyEvent.VK_ESCAPE) {
                // Request pause state
                if (eventBus != null) {
                    // This would typically be handled by the StateManager
                    GameLogger.info("Pause requested from playing state");
                }
            }
            
            // Other gameplay input is handled by the existing KeyHandler in GamePanel
        }
    }
    
    @Override
    public boolean pausesUnderlyingStates() {
        return false; // Playing state is typically the base state
    }
    
    @Override
    public boolean rendersOverUnderlyingStates() {
        return false; // Playing state renders the full game world
    }
}