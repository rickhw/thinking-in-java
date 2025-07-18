import rpg.systems.*;

/**
 * Test class to verify the event system functionality
 */
public class TestEventSystem {
    public static void main(String[] args) {
        System.out.println("Testing Event System...");
        
        // Create event bus
        EventBus eventBus = new EventBus();
        eventBus.setEventLogging(true);
        
        // Test event listeners
        TestEventListener listener1 = new TestEventListener("Listener1");
        TestEventListener listener2 = new TestEventListener("Listener2");
        
        // Subscribe to different event types
        eventBus.subscribe(CollisionEvent.class, listener1);
        eventBus.subscribe(InputEvent.class, listener1);
        eventBus.subscribe(CollisionEvent.class, listener2);
        
        System.out.println("Subscribed listeners");
        
        // Test publishing events
        System.out.println("\n--- Testing Event Publishing ---");
        
        // Test collision event
        CollisionEvent collisionEvent = new CollisionEvent(1, 2, CollisionEvent.CollisionType.ENTER);
        eventBus.publish(collisionEvent);
        
        // Test input event
        InputEvent inputEvent = new InputEvent(1, "move_up", InputEvent.InputType.PRESSED);
        eventBus.publish(inputEvent);
        
        // Test entity event
        EntityEvent entityEvent = new EntityEvent(1, EntityEvent.EntityEventType.CREATED);
        eventBus.publish(entityEvent);
        
        // Test game state event
        GameStateEvent stateEvent = new GameStateEvent("MENU", "PLAYING");
        eventBus.publish(stateEvent);
        
        // Test animation event
        AnimationEvent animEvent = new AnimationEvent(1, "walk", AnimationEvent.AnimationEventType.COMPLETED);
        eventBus.publish(animEvent);
        
        // Test movement event
        MovementEvent moveEvent = new MovementEvent(1, 10.0f, 20.0f, 15.0f, 25.0f);
        eventBus.publish(moveEvent);
        
        System.out.println("\n--- Processing Events ---");
        eventBus.processEvents();
        
        System.out.println("\n--- Testing Event Consumption ---");
        
        // Test event consumption
        ConsumingListener consumingListener = new ConsumingListener();
        eventBus.subscribe(CollisionEvent.class, consumingListener);
        
        CollisionEvent consumableEvent = new CollisionEvent(3, 4, CollisionEvent.CollisionType.ENTER);
        eventBus.publish(consumableEvent);
        eventBus.processEvents();
        
        System.out.println("\n--- Testing Immediate Events ---");
        
        // Test immediate event processing
        CollisionEvent immediateEvent = new CollisionEvent(5, 6, CollisionEvent.CollisionType.EXIT);
        eventBus.publishImmediate(immediateEvent);
        
        System.out.println("\n--- Testing Event Bus Statistics ---");
        System.out.println("Queued events: " + eventBus.getQueuedEventCount());
        System.out.println("Collision event listeners: " + eventBus.getListenerCount(CollisionEvent.class));
        System.out.println("Input event listeners: " + eventBus.getListenerCount(InputEvent.class));
        System.out.println("Has collision listeners: " + eventBus.hasListeners(CollisionEvent.class));
        System.out.println("Has movement listeners: " + eventBus.hasListeners(MovementEvent.class));
        
        System.out.println("\nEvent System Test Complete!");
    }
    
    static class TestEventListener implements EventListener<GameEvent> {
        private final String name;
        
        public TestEventListener(String name) {
            this.name = name;
        }
        
        @Override
        public void onEvent(GameEvent event) {
            System.out.println(name + " received: " + event.toString());
        }
    }
    
    static class ConsumingListener implements EventListener<CollisionEvent> {
        @Override
        public void onEvent(CollisionEvent event) {
            System.out.println("ConsumingListener received: " + event.toString());
            event.consume(); // Consume the event to prevent further processing
            System.out.println("Event consumed by ConsumingListener");
        }
    }
}