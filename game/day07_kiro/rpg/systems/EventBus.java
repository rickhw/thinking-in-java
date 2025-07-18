package rpg.systems;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Event bus for decoupled communication between game systems.
 * Supports event publishing, subscription, and queued event processing.
 * 
 * The EventBus is the central component of the event system, allowing systems
 * to communicate without direct dependencies. Systems can publish events and
 * subscribe to event types they're interested in.
 */
public class EventBus {
    // Map of event types to listeners
    private final Map<Class<? extends GameEvent>, List<EventListener<?>>> listeners;
    
    // Priority queue for regular events (processed on next update)
    private final Queue<PrioritizedEvent> eventQueue;
    
    // Priority queue for immediate events (processed right away)
    private final Queue<PrioritizedEvent> immediateEventQueue;
    
    // Event processing settings
    private boolean processEventsImmediately = false;
    private int maxEventsPerFrame = 100;
    private boolean enableEventLogging = false;
    private long eventTimeout = 5000; // 5 seconds default timeout
    
    /**
     * Create a new EventBus
     */
    public EventBus() {
        this.listeners = new ConcurrentHashMap<>();
        this.eventQueue = new PriorityBlockingQueue<>();
        this.immediateEventQueue = new PriorityBlockingQueue<>();
    }
    
    /**
     * Subscribe to events of a specific type
     * 
     * @param <T> The type of event to subscribe to
     * @param eventType The class of the event type
     * @param listener The listener to be notified when events of this type are published
     */
    public <T extends GameEvent> void subscribe(Class<T> eventType, EventListener<? super T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                 .add(listener);
        
        if (enableEventLogging) {
            System.out.println("EventBus: Subscribed to " + eventType.getSimpleName());
        }
    }
    
    /**
     * Unsubscribe from events of a specific type
     * 
     * @param <T> The type of event to unsubscribe from
     * @param eventType The class of the event type
     * @param listener The listener to remove
     */
    public <T extends GameEvent> void unsubscribe(Class<T> eventType, EventListener<? super T> listener) {
        List<EventListener<?>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            
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
     * 
     * @param event The event to publish
     */
    public void publish(GameEvent event) {
        if (event == null) return;
        
        if (processEventsImmediately) {
            immediateEventQueue.offer(new PrioritizedEvent(event));
            processImmediateEvents();
        } else {
            eventQueue.offer(new PrioritizedEvent(event));
        }
        
        if (enableEventLogging) {
            System.out.println("EventBus: Published " + event.getClass().getSimpleName() + 
                " with priority " + event.getPriority());
        }
    }
    
    /**
     * Publish an event to be processed immediately
     * 
     * @param event The event to publish immediately
     */
    public void publishImmediate(GameEvent event) {
        if (event == null) return;
        
        immediateEventQueue.offer(new PrioritizedEvent(event));
        processImmediateEvents();
        
        if (enableEventLogging) {
            System.out.println("EventBus: Published immediate " + event.getClass().getSimpleName() + 
                " with priority " + event.getPriority());
        }
    }
    
    /**
     * Process all queued events
     */
    public void processEvents() {
        int processedCount = 0;
        
        // Process events in priority order
        while (!eventQueue.isEmpty() && processedCount < maxEventsPerFrame) {
            PrioritizedEvent prioritizedEvent = eventQueue.poll();
            if (prioritizedEvent != null) {
                GameEvent event = prioritizedEvent.event;
                
                // Skip expired events
                if (event.isOlderThan(eventTimeout)) {
                    if (enableEventLogging) {
                        System.out.println("EventBus: Skipped expired event " + 
                            event.getClass().getSimpleName());
                    }
                    continue;
                }
                
                dispatchEvent(event);
                processedCount++;
            }
        }
        
        // Also process any immediate events
        processImmediateEvents();
    }
    
    /**
     * Process all immediate events
     */
    private void processImmediateEvents() {
        while (!immediateEventQueue.isEmpty()) {
            PrioritizedEvent prioritizedEvent = immediateEventQueue.poll();
            if (prioritizedEvent != null) {
                GameEvent event = prioritizedEvent.event;
                
                // Skip expired events
                if (event.isOlderThan(eventTimeout)) {
                    if (enableEventLogging) {
                        System.out.println("EventBus: Skipped expired immediate event " + 
                            event.getClass().getSimpleName());
                    }
                    continue;
                }
                
                dispatchEvent(event);
            }
        }
    }
    
    /**
     * Dispatch an event to all registered listeners
     * 
     * @param event The event to dispatch
     */
    @SuppressWarnings("unchecked")
    private void dispatchEvent(GameEvent event) {
        // Check for listeners for this exact event type
        List<EventListener<?>> exactListeners = listeners.get(event.getClass());
        if (exactListeners != null) {
            dispatchToListeners(event, exactListeners);
        }
        
        // Also check for listeners that might be listening to parent event types
        for (Map.Entry<Class<? extends GameEvent>, List<EventListener<?>>> entry : listeners.entrySet()) {
            Class<? extends GameEvent> listenerType = entry.getKey();
            
            // Skip if this is the exact type we already processed
            if (listenerType == event.getClass()) continue;
            
            // Check if this listener is for a parent type of our event
            if (listenerType.isAssignableFrom(event.getClass())) {
                dispatchToListeners(event, entry.getValue());
            }
        }
        
        if (enableEventLogging) {
            int listenerCount = exactListeners != null ? exactListeners.size() : 0;
            System.out.println("EventBus: Dispatched " + event.getClass().getSimpleName() + 
                " to " + listenerCount + " listeners" +
                (event.isConsumed() ? " (consumed)" : ""));
        }
    }
    
    /**
     * Dispatch an event to a specific list of listeners
     * 
     * @param event The event to dispatch
     * @param eventListeners The listeners to notify
     */
    @SuppressWarnings("unchecked")
    private void dispatchToListeners(GameEvent event, List<EventListener<?>> eventListeners) {
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
     * 
     * @return The number of events waiting to be processed
     */
    public int getQueuedEventCount() {
        return eventQueue.size();
    }
    
    /**
     * Get the number of listeners for a specific event type
     * 
     * @param eventType The class of the event type
     * @return The number of listeners registered for this event type
     */
    public int getListenerCount(Class<? extends GameEvent> eventType) {
        List<EventListener<?>> eventListeners = listeners.get(eventType);
        return eventListeners != null ? eventListeners.size() : 0;
    }
    
    /**
     * Set whether events should be processed immediately when published
     * 
     * @param immediate If true, events will be processed immediately when published
     */
    public void setProcessEventsImmediately(boolean immediate) {
        this.processEventsImmediately = immediate;
    }
    
    /**
     * Set the maximum number of events to process per frame
     * 
     * @param maxEvents The maximum number of events to process in a single frame
     */
    public void setMaxEventsPerFrame(int maxEvents) {
        this.maxEventsPerFrame = Math.max(1, maxEvents);
    }
    
    /**
     * Set the timeout for events in milliseconds
     * Events older than this will be discarded without processing
     * 
     * @param timeoutMs The timeout in milliseconds
     */
    public void setEventTimeout(long timeoutMs) {
        this.eventTimeout = timeoutMs;
    }
    
    /**
     * Enable or disable event logging for debugging
     * 
     * @param enabled If true, event processing will be logged to the console
     */
    public void setEventLogging(boolean enabled) {
        this.enableEventLogging = enabled;
    }
    
    /**
     * Check if there are any listeners for a specific event type
     * 
     * @param eventType The class of the event type
     * @return True if there are listeners for this event type, false otherwise
     */
    public boolean hasListeners(Class<? extends GameEvent> eventType) {
        List<EventListener<?>> eventListeners = listeners.get(eventType);
        return eventListeners != null && !eventListeners.isEmpty();
    }
    
    /**
     * Get all registered event types
     * 
     * @return A set of all event types that have listeners
     */
    public Set<Class<? extends GameEvent>> getRegisteredEventTypes() {
        return new HashSet<>(listeners.keySet());
    }
    
    /**
     * Helper class to store events with their priority for the priority queue
     */
    private static class PrioritizedEvent implements Comparable<PrioritizedEvent> {
        final GameEvent event;
        
        PrioritizedEvent(GameEvent event) {
            this.event = event;
        }
        
        @Override
        public int compareTo(PrioritizedEvent other) {
            // Higher priority events come first
            return Integer.compare(other.event.getPriority(), event.getPriority());
        }
    }
}