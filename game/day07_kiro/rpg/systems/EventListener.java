package rpg.systems;

/**
 * Interface for event listeners
 * 
 * @param <T> The type of GameEvent this listener handles
 */
@FunctionalInterface
public interface EventListener<T extends GameEvent> {
    /**
     * Called when an event of type T is published
     * 
     * @param event The event to handle
     */
    void onEvent(T event);
}