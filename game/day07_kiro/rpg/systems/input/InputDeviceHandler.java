package rpg.systems.input;

/**
 * Abstract base class for handling input from specific device types.
 * Provides a framework for device-specific input processing and configuration.
 */
public abstract class InputDeviceHandler {
    protected final InputDevice deviceType;
    protected boolean enabled;
    protected float sensitivity;
    
    /**
     * Create a new input device handler.
     * @param deviceType the type of device this handler manages
     */
    public InputDeviceHandler(InputDevice deviceType) {
        this.deviceType = deviceType;
        this.enabled = true;
        this.sensitivity = 1.0f;
    }
    
    /**
     * Initialize the device handler.
     * Called when the handler is first set up.
     */
    public abstract void initialize();
    
    /**
     * Update the device handler.
     * Called once per frame to process device input.
     * @param deltaTime the time since last update in seconds
     */
    public abstract void update(float deltaTime);
    
    /**
     * Cleanup the device handler.
     * Called when the handler is being destroyed.
     */
    public abstract void cleanup();
    
    /**
     * Check if the device is currently connected and available.
     * @return true if the device is available
     */
    public abstract boolean isDeviceAvailable();
    
    /**
     * Get the device type this handler manages.
     * @return the device type
     */
    public InputDevice getDeviceType() {
        return deviceType;
    }
    
    /**
     * Check if this handler is enabled.
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Set whether this handler is enabled.
     * @param enabled true to enable the handler
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Get the input sensitivity for this device.
     * @return the sensitivity multiplier
     */
    public float getSensitivity() {
        return sensitivity;
    }
    
    /**
     * Set the input sensitivity for this device.
     * @param sensitivity the sensitivity multiplier (typically 0.1 to 5.0)
     */
    public void setSensitivity(float sensitivity) {
        this.sensitivity = Math.max(0.1f, Math.min(5.0f, sensitivity));
    }
    
    /**
     * Get a human-readable status string for this device.
     * @return the status string
     */
    public String getStatusString() {
        return String.format("%s: %s (Sensitivity: %.1f)", 
            deviceType.getDisplayName(),
            isDeviceAvailable() ? (enabled ? "Enabled" : "Disabled") : "Not Available",
            sensitivity);
    }
}