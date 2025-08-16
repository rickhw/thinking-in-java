# Game State System Integration Example

This document shows how to integrate the new game state system with the existing GamePanel.

## Overview

The game state system provides:
- **GameState Interface**: Base interface for all game states
- **StateManager**: Manages state transitions and stack operations
- **Concrete States**: PlayingState, PausedState, MenuState, LoadingState
- **State Transitions**: Smooth transitions between states with visual effects
- **Event System**: State change notifications via EventBus

## Integration Steps

### 1. Modify GamePanel to Use State System

```java
public class GamePanel extends JPanel implements Runnable {
    // ... existing fields ...
    
    // Add state system
    private GameStateIntegration stateIntegration;
    
    public GamePanel() {
        // ... existing initialization ...
        
        // Initialize state system
        this.stateIntegration = new GameStateIntegration(this);
    }
    
    @Override
    public void run() {
        // ... existing game loop ...
        
        while (gameThread != null) {
            // ... timing code ...
            
            if (delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }
            
            // ... FPS counter ...
        }
    }
    
    public void update() {
        // Update state system first
        stateIntegration.update(1.0f / FPS);
        
        // Only update game logic if not paused
        if (stateIntegration.isPlaying()) {
            player.update();
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Always render the base game world
        tileManager.draw(g2);
        player.draw(g2);
        
        // Let state system render overlays (pause screen, menus, etc.)
        stateIntegration.render(g2);
        
        g2.dispose();
    }
}
```

### 2. Modify KeyHandler to Work with States

```java
public class KeyHandler implements KeyListener {
    // ... existing fields ...
    
    private GameStateIntegration stateIntegration;
    
    public void setStateIntegration(GameStateIntegration stateIntegration) {
        this.stateIntegration = stateIntegration;
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        // Let state system handle input first
        if (stateIntegration != null) {
            stateIntegration.handleKeyPressed(code);
        }
        
        // Only handle game input if playing
        if (stateIntegration == null || stateIntegration.isPlaying()) {
            // ... existing key handling for movement ...
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        
        if (stateIntegration != null) {
            stateIntegration.handleKeyReleased(code);
        }
        
        if (stateIntegration == null || stateIntegration.isPlaying()) {
            // ... existing key release handling ...
        }
    }
}
```

### 3. Usage Examples

#### Basic Pause/Unpause
```java
// In your game loop or input handler
if (escapePressed) {
    if (stateIntegration.isPlaying()) {
        // Pause the game
        stateIntegration.getStateManager().pushState("PAUSED");
    } else if (stateIntegration.isPaused()) {
        // Unpause the game
        stateIntegration.getStateManager().popState();
    }
}
```

#### Show Loading Screen
```java
// When loading a new level or assets
stateIntegration.showLoadingScreen();

// Update loading progress
LoadingState loadingState = (LoadingState) stateIntegration.getStateManager().getCurrentState();
loadingState.setProgress(0.5f); // 50% complete
loadingState.setStatus("Loading textures...");

// When loading is complete
stateIntegration.startNewGame();
```

#### Custom State Transitions
```java
// Add fade transition when changing states
StateManager stateManager = stateIntegration.getStateManager();
StateTransitionManager transitionManager = stateManager.getTransitionManager();

// Start fade transition
transitionManager.startFadeTransition(0.5f, Color.BLACK);

// Change state
stateManager.changeState("MENU");
```

#### Listen to State Changes
```java
EventBus eventBus = stateIntegration.getEventBus();
eventBus.subscribe(GameStateEvent.class, new EventListener<GameStateEvent>() {
    @Override
    public void onEvent(GameStateEvent event) {
        System.out.println("State changed: " + event.getPreviousState() + " -> " + event.getNewState());
        
        // Handle specific transitions
        if ("PLAYING".equals(event.getNewState())) {
            // Game started/resumed
            enableGameSystems();
        } else if ("PAUSED".equals(event.getNewState())) {
            // Game paused
            disableGameSystems();
        }
    }
});
```

## State Properties

### PlayingState
- **Pauses Underlying**: false (base game state)
- **Renders Over**: false (full screen game world)
- **Handles**: Game input, player movement, world interaction

### PausedState
- **Pauses Underlying**: true (stops game logic)
- **Renders Over**: true (overlay on game world)
- **Handles**: Unpause input (ESC key)

### MenuState
- **Pauses Underlying**: true (stops game logic)
- **Renders Over**: false (full screen menu)
- **Handles**: Menu navigation, selection

### LoadingState
- **Pauses Underlying**: true (stops everything)
- **Renders Over**: false (full screen loading)
- **Handles**: No input (loading only)

## Benefits

1. **Clean Separation**: Game logic and UI states are clearly separated
2. **Easy Pause System**: Automatic pause/resume without complex flags
3. **Extensible**: Easy to add new states (inventory, dialogue, etc.)
4. **Visual Effects**: Built-in transition animations
5. **Event-Driven**: Loose coupling through event system
6. **State Persistence**: Save/load game state for save systems

## Next Steps

1. Integrate with existing GamePanel
2. Add custom states for specific game features
3. Implement save/load functionality using state persistence
4. Add more transition effects
5. Create state-specific input handling systems