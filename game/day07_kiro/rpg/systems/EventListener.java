package rpg.systems;

/**
 * Interface for event listeners
 */
@FunctionalInterface
public interface EventListener<T extends GameEvent> {
    void onEvent(T event);
}