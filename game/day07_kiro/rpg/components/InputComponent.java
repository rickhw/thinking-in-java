package rpg.components;

import rpg.engine.Component;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Component that holds input bindings and state for an entity.
 * Allows entities to respond to keyboard and other input events.
 */
public class InputComponent extends Component {
    // Input bindings - maps actions to key codes
    private Map<String, Integer> keyBindings;
    
    // Current input state
    private Set<String> pressedActions;
    private Set<String> justPressedActions;
    private Set<String> justReleasedActions;
    
    // Input processing settings
    private boolean acceptInput;
    private int inputPriority;
    
    public InputComponent() {
        this.keyBindings = new HashMap<>();
        this.pressedActions = new HashSet<>();
        this.justPressedActions = new HashSet<>();
        this.justReleasedActions = new HashSet<>();
        this.acceptInput = true;
        this.inputPriority = 0;
        
        // Set up default key bindings
        setupDefaultBindings();
    }
    
    private void setupDefaultBindings() {
        // Default movement bindings
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
        keyBindings.put("attack", KeyEvent.VK_SPACE);
        keyBindings.put("run", KeyEvent.VK_SHIFT);
        keyBindings.put("menu", KeyEvent.VK_ESCAPE);
    }
    
    // Key binding methods
    public void bindKey(String action, int keyCode) {
        keyBindings.put(action, keyCode);
    }
    
    public void unbindKey(String action) {
        keyBindings.remove(action);
    }
    
    public Integer getKeyBinding(String action) {
        return keyBindings.get(action);
    }
    
    public Map<String, Integer> getAllBindings() {
        return new HashMap<>(keyBindings);
    }
    
    public void setBindings(Map<String, Integer> bindings) {
        this.keyBindings = new HashMap<>(bindings);
    }
    
    // Input state methods
    public boolean isActionPressed(String action) {
        return pressedActions.contains(action);
    }
    
    public boolean isActionJustPressed(String action) {
        return justPressedActions.contains(action);
    }
    
    public boolean isActionJustReleased(String action) {
        return justReleasedActions.contains(action);
    }
    
    // Convenience methods for common actions
    public boolean isMovingUp() {
        return isActionPressed("move_up") || isActionPressed("move_up_alt");
    }
    
    public boolean isMovingDown() {
        return isActionPressed("move_down") || isActionPressed("move_down_alt");
    }
    
    public boolean isMovingLeft() {
        return isActionPressed("move_left") || isActionPressed("move_left_alt");
    }
    
    public boolean isMovingRight() {
        return isActionPressed("move_right") || isActionPressed("move_right_alt");
    }
    
    public boolean isMoving() {
        return isMovingUp() || isMovingDown() || isMovingLeft() || isMovingRight();
    }
    
    public boolean isRunning() {
        return isActionPressed("run");
    }
    
    public boolean isInteracting() {
        return isActionJustPressed("interact");
    }
    
    public boolean isAttacking() {
        return isActionJustPressed("attack");
    }
    
    // Input processing
    public void processKeyPressed(int keyCode) {
        if (!acceptInput) return;
        
        String action = getActionForKey(keyCode);
        if (action != null) {
            if (!pressedActions.contains(action)) {
                justPressedActions.add(action);
            }
            pressedActions.add(action);
        }
    }
    
    public void processKeyReleased(int keyCode) {
        if (!acceptInput) return;
        
        String action = getActionForKey(keyCode);
        if (action != null) {
            pressedActions.remove(action);
            justReleasedActions.add(action);
        }
    }
    
    private String getActionForKey(int keyCode) {
        for (Map.Entry<String, Integer> entry : keyBindings.entrySet()) {
            if (entry.getValue() == keyCode) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    // Input settings
    public boolean isAcceptingInput() {
        return acceptInput;
    }
    
    public void setAcceptInput(boolean acceptInput) {
        this.acceptInput = acceptInput;
        if (!acceptInput) {
            // Clear all input state when disabling input
            pressedActions.clear();
            justPressedActions.clear();
            justReleasedActions.clear();
        }
    }
    
    public int getInputPriority() {
        return inputPriority;
    }
    
    public void setInputPriority(int priority) {
        this.inputPriority = priority;
    }
    
    // Get movement vector based on current input
    public float[] getMovementVector() {
        float x = 0;
        float y = 0;
        
        if (isMovingLeft()) x -= 1;
        if (isMovingRight()) x += 1;
        if (isMovingUp()) y -= 1;
        if (isMovingDown()) y += 1;
        
        // Normalize diagonal movement
        if (x != 0 && y != 0) {
            float length = (float) Math.sqrt(x * x + y * y);
            x /= length;
            y /= length;
        }
        
        return new float[]{x, y};
    }
    
    @Override
    public void update(float deltaTime) {
        // Clear just pressed/released actions at the end of each frame
        justPressedActions.clear();
        justReleasedActions.clear();
    }
}