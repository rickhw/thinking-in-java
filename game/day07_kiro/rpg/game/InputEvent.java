package rpg.game;

/**
 * Represents an input event that can be handled by game states.
 */
public class InputEvent {
    
    public enum Type {
        KEY_PRESSED,
        KEY_RELEASED,
        KEY_HELD,
        MOUSE_PRESSED,
        MOUSE_RELEASED,
        MOUSE_MOVED
    }
    
    private final Type type;
    private final int keyCode;
    private final int mouseX;
    private final int mouseY;
    private final int mouseButton;
    private final long timestamp;
    
    /**
     * Create a keyboard input event.
     */
    public InputEvent(Type type, int keyCode) {
        this.type = type;
        this.keyCode = keyCode;
        this.mouseX = 0;
        this.mouseY = 0;
        this.mouseButton = 0;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Create a mouse input event.
     */
    public InputEvent(Type type, int mouseX, int mouseY, int mouseButton) {
        this.type = type;
        this.keyCode = 0;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.mouseButton = mouseButton;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Type getType() { return type; }
    public int getKeyCode() { return keyCode; }
    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
    public int getMouseButton() { return mouseButton; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("InputEvent{type=%s, keyCode=%d, mouseX=%d, mouseY=%d, mouseButton=%d, timestamp=%d}",
            type, keyCode, mouseX, mouseY, mouseButton, timestamp);
    }
}