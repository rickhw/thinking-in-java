package rpg.systems.commands;

import rpg.engine.Entity;

/**
 * Base class for input-related commands.
 * Provides common functionality for commands triggered by input events.
 */
public abstract class InputCommand implements Command {
    protected final Entity entity;
    protected boolean justPressed;
    protected boolean justReleased;
    protected long timestamp;
    
    /**
     * Create a new input command for the specified entity.
     * @param entity the entity this command affects
     */
    public InputCommand(Entity entity) {
        this.entity = entity;
        this.justPressed = false;
        this.justReleased = false;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Set whether this command was triggered by a key press (as opposed to key hold).
     * @param justPressed true if this is a key press event
     */
    public void setJustPressed(boolean justPressed) {
        this.justPressed = justPressed;
    }
    
    /**
     * Set whether this command was triggered by a key release.
     * @param justReleased true if this is a key release event
     */
    public void setJustReleased(boolean justReleased) {
        this.justReleased = justReleased;
    }
    
    /**
     * Get the entity this command affects.
     * @return the target entity
     */
    public Entity getEntity() {
        return entity;
    }
    
    /**
     * Check if this command was triggered by a key press.
     * @return true if this is a key press event
     */
    public boolean isJustPressed() {
        return justPressed;
    }
    
    /**
     * Check if this command was triggered by a key release.
     * @return true if this is a key release event
     */
    public boolean isJustReleased() {
        return justReleased;
    }
    
    /**
     * Get the timestamp when this command was created.
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Check if this command should be executed based on input state.
     * Override in subclasses to implement specific logic.
     * @return true if the command should execute
     */
    protected boolean shouldExecute() {
        return entity != null && entity.isActive();
    }
    
    @Override
    public final void execute() {
        if (shouldExecute()) {
            executeInput();
        }
    }
    
    /**
     * Execute the input-specific logic.
     * Subclasses should implement this method instead of execute().
     */
    protected abstract void executeInput();
    
    @Override
    public String getDescription() {
        return String.format("%s(entity=%d, justPressed=%b, justReleased=%b)", 
            getClass().getSimpleName(), 
            entity != null ? entity.getId() : -1, 
            justPressed, 
            justReleased);
    }
}