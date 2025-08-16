package rpg.systems.commands;

import rpg.engine.Entity;

/**
 * Factory interface for creating commands.
 * Allows for flexible command creation based on entities and context.
 */
@FunctionalInterface
public interface CommandFactory {
    /**
     * Create a command for the given entity.
     * @param entity the entity that will be affected by the command
     * @return a new command instance, or null if no command should be created
     */
    Command createCommand(Entity entity);
}