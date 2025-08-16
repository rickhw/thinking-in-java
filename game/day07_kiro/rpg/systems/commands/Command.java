package rpg.systems.commands;

/**
 * Base interface for all commands in the command pattern.
 * Commands encapsulate actions that can be executed and potentially undone.
 */
public interface Command {
    /**
     * Execute the command.
     */
    void execute();
    
    /**
     * Undo the command if possible.
     * Not all commands support undo functionality.
     */
    default void undo() {
        // Default implementation does nothing
        // Override in subclasses that support undo
    }
    
    /**
     * Check if this command can be undone.
     * @return true if the command supports undo functionality
     */
    default boolean canUndo() {
        return false;
    }
    
    /**
     * Get a description of what this command does.
     * Useful for debugging and logging.
     * @return description of the command
     */
    default String getDescription() {
        return getClass().getSimpleName();
    }
}