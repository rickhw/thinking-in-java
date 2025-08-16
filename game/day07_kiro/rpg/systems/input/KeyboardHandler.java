package rpg.systems.input;

/**
 * Input device handler for keyboard input.
 * Manages keyboard-specific settings and processing.
 */
public class KeyboardHandler extends InputDeviceHandler {
    private int keyRepeatDelay;
    private int keyRepeatRate;
    private boolean capsLockState;
    private boolean numLockState;
    private boolean scrollLockState;
    
    /**
     * Create a new keyboard handler.
     */
    public KeyboardHandler() {
        super(InputDevice.KEYBOARD);
        this.keyRepeatDelay = 250; // milliseconds
        this.keyRepeatRate = 50;   // milliseconds between repeats
    }
    
    @Override
    public void initialize() {
        // Initialize keyboard state
        updateLockStates();
    }
    
    @Override
    public void update(float deltaTime) {
        if (!enabled) return;
        
        // Update lock key states
        updateLockStates();
        
        // Handle key repeat logic if needed
        // This would be implemented based on specific requirements
    }
    
    @Override
    public void cleanup() {
        // No specific cleanup needed for keyboard
    }
    
    @Override
    public boolean isDeviceAvailable() {
        // Keyboard is always available in a desktop environment
        return true;
    }
    
    /**
     * Update the state of lock keys (Caps Lock, Num Lock, Scroll Lock).
     */
    private void updateLockStates() {
        try {
            // Get lock key states from the system
            java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
            capsLockState = toolkit.getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK);
            numLockState = toolkit.getLockingKeyState(java.awt.event.KeyEvent.VK_NUM_LOCK);
            scrollLockState = toolkit.getLockingKeyState(java.awt.event.KeyEvent.VK_SCROLL_LOCK);
        } catch (Exception e) {
            // Some systems may not support getting lock key states
            // This is not critical for game functionality
        }
    }
    
    /**
     * Get the key repeat delay in milliseconds.
     * @return the delay before key repeat starts
     */
    public int getKeyRepeatDelay() {
        return keyRepeatDelay;
    }
    
    /**
     * Set the key repeat delay.
     * @param keyRepeatDelay the delay in milliseconds
     */
    public void setKeyRepeatDelay(int keyRepeatDelay) {
        this.keyRepeatDelay = Math.max(0, keyRepeatDelay);
    }
    
    /**
     * Get the key repeat rate in milliseconds.
     * @return the time between key repeats
     */
    public int getKeyRepeatRate() {
        return keyRepeatRate;
    }
    
    /**
     * Set the key repeat rate.
     * @param keyRepeatRate the time between repeats in milliseconds
     */
    public void setKeyRepeatRate(int keyRepeatRate) {
        this.keyRepeatRate = Math.max(10, keyRepeatRate);
    }
    
    /**
     * Check if Caps Lock is currently active.
     * @return true if Caps Lock is on
     */
    public boolean isCapsLockOn() {
        return capsLockState;
    }
    
    /**
     * Check if Num Lock is currently active.
     * @return true if Num Lock is on
     */
    public boolean isNumLockOn() {
        return numLockState;
    }
    
    /**
     * Check if Scroll Lock is currently active.
     * @return true if Scroll Lock is on
     */
    public boolean isScrollLockOn() {
        return scrollLockState;
    }
    
    @Override
    public String getStatusString() {
        return String.format("%s (Caps: %s, Num: %s, Scroll: %s)", 
            super.getStatusString(),
            capsLockState ? "ON" : "OFF",
            numLockState ? "ON" : "OFF",
            scrollLockState ? "ON" : "OFF");
    }
}