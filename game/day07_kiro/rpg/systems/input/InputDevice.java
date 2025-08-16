package rpg.systems.input;

/**
 * Enumeration of supported input device types.
 * Used for device-specific input handling and configuration.
 */
public enum InputDevice {
    /**
     * Standard keyboard input device.
     */
    KEYBOARD("Keyboard"),
    
    /**
     * Standard mouse input device.
     */
    MOUSE("Mouse"),
    
    /**
     * Gamepad/controller input device.
     */
    GAMEPAD("Gamepad"),
    
    /**
     * Touch screen input device.
     */
    TOUCH("Touch Screen"),
    
    /**
     * Custom or unknown input device.
     */
    CUSTOM("Custom Device");
    
    private final String displayName;
    
    /**
     * Create an input device enum value.
     * @param displayName the human-readable name
     */
    InputDevice(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Get the human-readable display name.
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Check if this device supports analog input.
     * @return true if the device supports analog input
     */
    public boolean supportsAnalogInput() {
        return this == GAMEPAD || this == TOUCH;
    }
    
    /**
     * Check if this device supports multiple simultaneous inputs.
     * @return true if the device supports multiple inputs
     */
    public boolean supportsMultipleInputs() {
        return this == KEYBOARD || this == GAMEPAD || this == TOUCH;
    }
    
    /**
     * Get the maximum number of buttons/keys this device typically has.
     * @return the maximum button count, or -1 if unlimited
     */
    public int getMaxButtonCount() {
        switch (this) {
            case KEYBOARD: return 256; // Standard keyboard key codes
            case MOUSE: return 8; // Typical mouse button count
            case GAMEPAD: return 32; // Typical gamepad button count
            case TOUCH: return -1; // Unlimited touch points
            case CUSTOM: return -1; // Unknown
            default: return -1;
        }
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}