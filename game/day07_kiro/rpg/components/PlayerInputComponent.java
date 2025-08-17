package rpg.components;

import rpg.engine.Component;
import rpg.entity.Direction;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Player-specific input component that handles player input and direction management.
 * Extends the basic InputComponent with player-specific functionality.
 */
public class PlayerInputComponent extends Component {
    private static final long serialVersionUID = 1L;
    
    // Input bindings - maps actions to key codes
    private Map<String, Integer> keyBindings;
    
    // Current input state
    private boolean upPressed;
    private boolean downPressed;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean runPressed;
    private boolean interactPressed;
    
    // Player direction
    private Direction direction;
    private Direction lastDirection;
    
    // Input processing settings
    private boolean acceptInput;
    
    public PlayerInputComponent() {
        this.keyBindings = new HashMap<>();
        this.direction = Direction.DOWN;
        this.lastDirection = Direction.DOWN;
        this.acceptInput = true;
        
        // Set up default key bindings for player
        setupDefaultBindings();
    }
    
    private void setupDefaultBindings() {
        // Movement bindings (WASD and arrow keys)
        keyBindings.put("move_up", KeyEvent.VK_W);
        keyBindings.put("move_down", KeyEvent.VK_S);
        keyBindings.put("move_left", KeyEvent.VK_A);
        keyBindings.put("move_right", KeyEvent.VK_D);
        
        // Alternative arrow key bindings
        keyBindings.put("move_up_alt", KeyEvent.VK_UP);
        keyBindings.put("move_down_alt", KeyEvent.VK_DOWN);
        keyBindings.put("move_left_alt", KeyEvent.VK_LEFT);
        keyBindings.put("move_right_alt", KeyEvent.VK_RIGHT);
        
        // Action bindings
        keyBindings.put("interact", KeyEvent.VK_E);
        keyBindings.put("run", KeyEvent.VK_SHIFT);
    }
    
    // Key binding methods
    public void bindKey(String action, int keyCode) {
        keyBindings.put(action, keyCode);
    }
    
    public Integer getKeyBinding(String action) {
        return keyBindings.get(action);
    }
    
    // Input processing methods
    public void processKeyPressed(int keyCode) {
        if (!acceptInput) return;
        
        if (keyCode == keyBindings.get("move_up") || keyCode == keyBindings.get("move_up_alt")) {
            upPressed = true;
        } else if (keyCode == keyBindings.get("move_down") || keyCode == keyBindings.get("move_down_alt")) {
            downPressed = true;
        } else if (keyCode == keyBindings.get("move_left") || keyCode == keyBindings.get("move_left_alt")) {
            leftPressed = true;
        } else if (keyCode == keyBindings.get("move_right") || keyCode == keyBindings.get("move_right_alt")) {
            rightPressed = true;
        } else if (keyCode == keyBindings.get("run")) {
            runPressed = true;
        } else if (keyCode == keyBindings.get("interact")) {
            interactPressed = true;
        }
        
        updateDirection();
    }
    
    public void processKeyReleased(int keyCode) {
        if (!acceptInput) return;
        
        if (keyCode == keyBindings.get("move_up") || keyCode == keyBindings.get("move_up_alt")) {
            upPressed = false;
        } else if (keyCode == keyBindings.get("move_down") || keyCode == keyBindings.get("move_down_alt")) {
            downPressed = false;
        } else if (keyCode == keyBindings.get("move_left") || keyCode == keyBindings.get("move_left_alt")) {
            leftPressed = false;
        } else if (keyCode == keyBindings.get("move_right") || keyCode == keyBindings.get("move_right_alt")) {
            rightPressed = false;
        } else if (keyCode == keyBindings.get("run")) {
            runPressed = false;
        } else if (keyCode == keyBindings.get("interact")) {
            interactPressed = false;
        }
        
        updateDirection();
    }
    
    private void updateDirection() {
        // Update direction based on current input
        // Priority: most recently pressed direction
        if (upPressed) {
            direction = Direction.UP;
        } else if (downPressed) {
            direction = Direction.DOWN;
        } else if (leftPressed) {
            direction = Direction.LEFT;
        } else if (rightPressed) {
            direction = Direction.RIGHT;
        }
        
        // Store last direction for when no movement keys are pressed
        if (isMoving()) {
            lastDirection = direction;
        }
    }
    
    // Input state getters
    public boolean isUpPressed() {
        return upPressed;
    }
    
    public boolean isDownPressed() {
        return downPressed;
    }
    
    public boolean isLeftPressed() {
        return leftPressed;
    }
    
    public boolean isRightPressed() {
        return rightPressed;
    }
    
    public boolean isRunPressed() {
        return runPressed;
    }
    
    public boolean isInteractPressed() {
        return interactPressed;
    }
    
    public boolean isMoving() {
        return upPressed || downPressed || leftPressed || rightPressed;
    }
    
    // Direction methods
    public Direction getDirection() {
        return direction;
    }
    
    public Direction getLastDirection() {
        return lastDirection;
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
        this.lastDirection = direction;
    }
    
    // Get movement vector based on current input
    public float[] getMovementVector() {
        float x = 0;
        float y = 0;
        
        if (leftPressed) x -= 1;
        if (rightPressed) x += 1;
        if (upPressed) y -= 1;
        if (downPressed) y += 1;
        
        // Normalize diagonal movement
        if (x != 0 && y != 0) {
            float length = (float) Math.sqrt(x * x + y * y);
            x /= length;
            y /= length;
        }
        
        return new float[]{x, y};
    }
    
    // Input settings
    public boolean isAcceptingInput() {
        return acceptInput;
    }
    
    public void setAcceptInput(boolean acceptInput) {
        this.acceptInput = acceptInput;
        if (!acceptInput) {
            // Clear all input state when disabling input
            upPressed = false;
            downPressed = false;
            leftPressed = false;
            rightPressed = false;
            runPressed = false;
            interactPressed = false;
        }
    }
    
    @Override
    public void update(float deltaTime) {
        // Reset one-time actions
        interactPressed = false;
    }
}