package rpg.systems;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event bus for decoupled communication between game systems.
 * Supports event publishing, subscription, and queued event processing.
 */
public class EventBus {
    private final Map<Class<? extends GameEvent>, List<EventListener<?>>> listeners;
    private final Queue<GameEvent> eventQueue;
    private final Queue<GameEvent> immediateEventQueue;
    
    // Event processing settings
    private boolean processEventsImmediately = false;
    private int maxEventsPerFrame = 100;
    private boolean enableEventLogging = false;
    
    public EventBus() {
        this.listeners = new ConcurrentHashMap<>();
        this.eventQueue = new LinkedList<>();
        this.immediateEventQueue = new LinkedList<>();
    }
    
    /**
     * Subscribe to events of a specific type
     */
    @SuppressWarnings("unchecked")
    public <T extends GameEvent> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                 .add((EventListener<GameEvent>) listener);
        
        if (enableEventLogging) {
            System.out.println("EventBus: Subscribed to " + eventType.getSimpleName());
        }
    }
    
    /**
     * Unsubscribe from events of a specific type
     */
    @SuppressWarnings("unchecked")
    public <T extends GameEvent> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        List<EventListener<?>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove((EventListener<GameEvent>) listener);
            
            // Remove empty listener lists
            if (eventListeners.isEmpty()) {
                listeners.remove(eventType);
            }
        }
        
        if (enableEventLogging) {
            System.out.println("EventBus: Unsubscribed from " + eventType.getSimpleName());
        }
    }
    
    /**
     * Publish an event to be processed in the next frame
     */
    public void publish(GameEvent event) {
        if (event == null) return;
        
        if (processEventsImmediately) {
            immediateEventQueue.offer(event);
            processImmediateEvents();
        } else {
            eventQueue.offer(event);
        }
        
        if (enableEventLogging) {
            System.out.println("EventBus: Published " + event.getClass().getSimpleName());
        }
    }
    
    /**
     * Publish an event to be processed immediately
     */
    public void publishImmediate(GameEvent event) {
        if (event == null) return;
        
        immediateEventQueue.offer(event);
        processImmediateEvents();
        
        if (enableEventLogging) {
            System.out.println("EventBus: Published immediate " + event.getClass().getSimpleName());
        }
    }
    
    /**
     * Process all queued events
     */
    public void processEvents() {
        int processedCount = 0;
        
        while (!eventQueue.isEmpty() && processedCount < maxEventsPerFrame) {
            GameEvent event = eventQueue.poll();
            if (event != null) {
                dispatchEvent(event);
                processedCount++;
            }
        }
        
        // Also process any immediate events
        processImmediateEvents();
    }
    
    private void processImmediateEvents() {
        while (!immediateEventQueue.isEmpty()) {
            GameEvent event = immediateEventQueue.poll();
            if (event != null) {
                dispatchEvent(event);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void dispatchEvent(GameEvent event) {
        List<EventListener<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener<?> listener : eventListeners) {
                try {
                    ((EventListener<GameEvent>) listener).onEvent(event);
                } catch (Exception e) {
                    System.err.println("EventBus: Error processing event " + 
                        event.getClass().getSimpleName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        if (enableEventLogging) {
            System.out.println("EventBus: Dispatched " + event.getClass().getSimpleName() + 
                " to " + (eventListeners != null ? eventListeners.size() : 0) + " listeners");
        }
    }
    
    /**
     * Clear all events and listeners
     */
    public void clear() {
        eventQueue.clear();
        immediateEventQueue.clear();
        listeners.clear();
    }
    
    /**
     * Get the number of queued events
     */
    public int getQueuedEventCount() {
        return eventQueue.size();
    }
    
    /**
     * Get the number of listeners for a specific event type
     */
    public int getListenerCount(Class<? extends GameEvent> eventType) {
        List<EventListener<?>> eventListeners = listeners.get(eventType);
        return eventListeners != null ? eventListeners.size() : 0;
    }
    
    /**
     * Set whether events should be processed immediately when published
     */
    public void setProcessEventsImmediately(boolean immediate) {
        this.processEventsImmediately = immediate;
    }
    
    /**
     * Set the maximum number of events to process per frame
     */
    public void setMaxEventsPerFrame(int maxEvents) {
        this.maxEventsPerFrame = Math.max(1, maxEvents);
    }
    
    /**
     * Enable or disable event logging for debugging
     */
    public void setEventLogging(boolean enabled) {
        this.enableEventLogging = enabled;
    }
    
    /**
     * Check if there are any listeners for a specific event type
     */
    public boolean hasListeners(Class<? extends GameEvent> eventType) {
        List<EventListener<?>> eventListeners = listeners.get(eventType);
        return eventListeners != null && !eventListeners.isEmpty();
    }
}
/**

 * Base class for all game events
 */
abstract class GameEvent {
    private final long timestamp;
    private boolean consumed = false;
    
    public GameEvent() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get the timestamp when this event was created
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Mark this event as consumed (prevents further processing)
     */
    public void consume() {
        this.consumed = true;
    }
    
    /**
     * Check if this event has been consumed
     */
    public boolean isConsumed() {
        return consumed;
    }
}

/**
 * Interface for event listeners
 */
@FunctionalInterface
interface EventListener<T extends GameEvent> {
    void onEvent(T event);
}

// Common game event types

/**
 * Event fired when entities collide
 */
class CollisionEvent extends GameEvent {
    private final int entityA;
    private final int entityB;
    private final CollisionType type;
    
    public enum CollisionType {
        ENTER,  // Collision started
        STAY,   // Collision continuing
        EXIT    // Collision ended
    }
    
    public CollisionEvent(int entityA, int entityB, CollisionType type) {
        super();
        this.entityA = entityA;
        this.entityB = entityB;
        this.type = type;
    }
    
    public int getEntityA() { return entityA; }
    public int getEntityB() { return entityB; }
    public CollisionType getType() { return type; }
}

/**
 * Event fired when input actions occur
 */
class InputEvent extends GameEvent {
    private final int entityId;
    private final String action;
    private final InputType type;
    
    public enum InputType {
        PRESSED,
        RELEASED,
        HELD
    }
    
    public InputEvent(int entityId, String action, InputType type) {
        super();
        this.entityId = entityId;
        this.action = action;
        this.type = type;
    }
    
    public int getEntityId() { return entityId; }
    public String getAction() { return action; }
    public InputType getType() { return type; }
}

/**
 * Event fired when entities are created or destroyed
 */
class EntityEvent extends GameEvent {
    private final int entityId;
    private final EntityEventType type;
    
    public enum EntityEventType {
        CREATED,
        DESTROYED,
        COMPONENT_ADDED,
        COMPONENT_REMOVED
    }
    
    public EntityEvent(int entityId, EntityEventType type) {
        super();
        this.entityId = entityId;
        this.type = type;
    }
    
    public int getEntityId() { return entityId; }
    public EntityEventType getType() { return type; }
}

/**
 * Event fired when game state changes
 */
class GameStateEvent extends GameEvent {
    private final String previousState;
    private final String newState;
    
    public GameStateEvent(String previousState, String newState) {
        super();
        this.previousState = previousState;
        this.newState = newState;
    }
    
    public String getPreviousState() { return previousState; }
    public String getNewState() { return newState; }
}

/**
 * Event fired when animations complete
 */
class AnimationEvent extends GameEvent {
    private final int entityId;
    private final String animationName;
    private final AnimationEventType type;
    
    public enum AnimationEventType {
        STARTED,
        COMPLETED,
        LOOPED
    }
    
    public AnimationEvent(int entityId, String animationName, AnimationEventType type) {
        super();
        this.entityId = entityId;
        this.animationName = animationName;
        this.type = type;
    }
    
    public int getEntityId() { return entityId; }
    public String getAnimationName() { return animationName; }
    public AnimationEventType getType() { return type; }
}

/**
 * Event fired for movement-related notifications
 */
class MovementEvent extends GameEvent {
    private final int entityId;
    private final float oldX, oldY;
    private final float newX, newY;
    
    public MovementEvent(int entityId, float oldX, float oldY, float newX, float newY) {
        super();
        this.entityId = entityId;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = newX;
        this.newY = newY;
    }
    
    public int getEntityId() { return entityId; }
    public float getOldX() { return oldX; }
    public float getOldY() { return oldY; }
    public float getNewX() { return newX; }
    public float getNewY() { return newY; }
    
    public float getDistanceMoved() {
        float dx = newX - oldX;
        float dy = newY - oldY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}