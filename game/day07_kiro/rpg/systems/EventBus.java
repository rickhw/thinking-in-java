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
                // Skip processing if event has been consumed
                if (event.isConsumed()) {
                    break;
                }
                
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
                " to " + (eventListeners != null ? eventListeners.size() : 0) + " listeners" +
                (event.isConsumed() ? " (consumed)" : ""));
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