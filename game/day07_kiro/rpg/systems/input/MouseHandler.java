package rpg.systems.input;

import java.awt.Point;
import java.awt.MouseInfo;

/**
 * Input device handler for mouse input.
 * Manages mouse-specific settings and processing including position tracking and sensitivity.
 */
public class MouseHandler extends InputDeviceHandler {
    private Point currentPosition;
    private Point previousPosition;
    private int wheelRotation;
    private int doubleClickTime;
    private boolean invertY;
    
    /**
     * Create a new mouse handler.
     */
    public MouseHandler() {
        super(InputDevice.MOUSE);
        this.currentPosition = new Point(0, 0);
        this.previousPosition = new Point(0, 0);
        this.wheelRotation = 0;
        this.doubleClickTime = 300; // milliseconds
        this.invertY = false;
    }
    
    @Override
    public void initialize() {
        // Initialize mouse position
        updateMousePosition();
        previousPosition.setLocation(currentPosition);
    }
    
    @Override
    public void update(float deltaTime) {
        if (!enabled) return;
        
        // Update mouse position
        previousPosition.setLocation(currentPosition);
        updateMousePosition();
    }
    
    @Override
    public void cleanup() {
        // No specific cleanup needed for mouse
    }
    
    @Override
    public boolean isDeviceAvailable() {
        // Mouse is typically always available in a desktop environment
        return true;
    }
    
    /**
     * Update the current mouse position from the system.
     */
    private void updateMousePosition() {
        try {
            Point systemPosition = MouseInfo.getPointerInfo().getLocation();
            currentPosition.setLocation(systemPosition);
        } catch (Exception e) {
            // If we can't get mouse position, keep the previous position
        }
    }
    
    /**
     * Get the current mouse position.
     * @return the current mouse position
     */
    public Point getCurrentPosition() {
        return new Point(currentPosition);
    }
    
    /**
     * Get the previous mouse position.
     * @return the previous mouse position
     */
    public Point getPreviousPosition() {
        return new Point(previousPosition);
    }
    
    /**
     * Get the mouse movement delta since last update.
     * @return the movement delta as a Point
     */
    public Point getMovementDelta() {
        int deltaX = currentPosition.x - previousPosition.x;
        int deltaY = currentPosition.y - previousPosition.y;
        
        // Apply sensitivity
        deltaX = (int) (deltaX * sensitivity);
        deltaY = (int) (deltaY * sensitivity * (invertY ? -1 : 1));
        
        return new Point(deltaX, deltaY);
    }
    
    /**
     * Get the current wheel rotation value.
     * @return the wheel rotation (positive = up, negative = down)
     */
    public int getWheelRotation() {
        return wheelRotation;
    }
    
    /**
     * Set the wheel rotation value.
     * This is typically called by the mouse wheel event handler.
     * @param wheelRotation the wheel rotation value
     */
    public void setWheelRotation(int wheelRotation) {
        this.wheelRotation = wheelRotation;
    }
    
    /**
     * Reset the wheel rotation to zero.
     * Should be called after processing wheel events.
     */
    public void resetWheelRotation() {
        this.wheelRotation = 0;
    }
    
    /**
     * Get the double-click time threshold in milliseconds.
     * @return the double-click time
     */
    public int getDoubleClickTime() {
        return doubleClickTime;
    }
    
    /**
     * Set the double-click time threshold.
     * @param doubleClickTime the time in milliseconds
     */
    public void setDoubleClickTime(int doubleClickTime) {
        this.doubleClickTime = Math.max(50, Math.min(1000, doubleClickTime));
    }
    
    /**
     * Check if Y-axis is inverted.
     * @return true if Y-axis is inverted
     */
    public boolean isYInverted() {
        return invertY;
    }
    
    /**
     * Set whether to invert the Y-axis.
     * @param invertY true to invert Y-axis
     */
    public void setYInverted(boolean invertY) {
        this.invertY = invertY;
    }
    
    /**
     * Calculate the distance moved since last update.
     * @return the distance in pixels
     */
    public double getMovementDistance() {
        Point delta = getMovementDelta();
        return Math.sqrt(delta.x * delta.x + delta.y * delta.y);
    }
    
    /**
     * Calculate the movement angle since last update.
     * @return the angle in radians (0 = right, PI/2 = up, PI = left, 3*PI/2 = down)
     */
    public double getMovementAngle() {
        Point delta = getMovementDelta();
        return Math.atan2(-delta.y, delta.x); // Negative Y because screen coordinates are inverted
    }
    
    @Override
    public String getStatusString() {
        Point delta = getMovementDelta();
        return String.format("%s (Pos: %d,%d, Delta: %d,%d, Wheel: %d)", 
            super.getStatusString(),
            currentPosition.x, currentPosition.y,
            delta.x, delta.y,
            wheelRotation);
    }
}