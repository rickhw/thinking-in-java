package rpg.systems;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for game systems that handles registration, initialization, and execution order.
 * Provides centralized control over all game systems and their lifecycle.
 * 
 * The SystemManager is responsible for:
 * - Registering and unregistering game systems
 * - Initializing systems in the correct order
 * - Updating systems each frame in priority order
 * - Managing system dependencies
 * - Handling system lifecycle events
 */
public class SystemManager {
    private final Map<Class<? extends GameSystem>, GameSystem> systems;
    private final List<GameSystem> updateOrder;
    private final EventBus eventBus;
    private final Map<Class<? extends GameSystem>, Set<Class<? extends GameSystem>>> dependencies;
    
    // System management settings
    private boolean initialized = false;
    private boolean enableSystemProfiling = false;
    private Map<Class<? extends GameSystem>, SystemProfile> systemProfiles;
    
    /**
     * Create a new SystemManager with the specified EventBus
     * 
     * @param eventBus The EventBus to use for system communication
     */
    public SystemManager(EventBus eventBus) {
        this.systems = new ConcurrentHashMap<>();
        this.updateOrder = new ArrayList<>();
        this.eventBus = eventBus;
        this.systemProfiles = new HashMap<>();
        this.dependencies = new HashMap<>();
    }
    
    /**
     * Register a system with the manager
     * 
     * @param <T> The type of system to register
     * @param system The system instance to register
     */
    public <T extends GameSystem> void registerSystem(T system) {
        if (system == null) {
            throw new IllegalArgumentException("System cannot be null");
        }
        
        Class<? extends GameSystem> systemClass = system.getClass();
        
        if (systems.containsKey(systemClass)) {
            throw new IllegalStateException("System " + systemClass.getSimpleName() + " is already registered");
        }
        
        systems.put(systemClass, system);
        
        // Insert system in correct position based on priority
        insertSystemInOrder(system);
        
        // Initialize system if manager is already initialized
        if (initialized) {
            system.initialize();
            
            // Publish system registration event
            eventBus.publish(new SystemEvent(systemClass.getName(), SystemEvent.SystemEventType.REGISTERED));
        }
        
        // Create profiling data if enabled
        if (enableSystemProfiling) {
            systemProfiles.put(systemClass, new SystemProfile(systemClass.getSimpleName()));
        }
        
        System.out.println("SystemManager: Registered " + systemClass.getSimpleName());
    }
    
    /**
     * Register a system with dependencies
     * 
     * @param <T> The type of system to register
     * @param system The system instance to register
     * @param dependsOn The system classes this system depends on
     */
    @SafeVarargs
    public final <T extends GameSystem> void registerSystemWithDependencies(
            T system, Class<? extends GameSystem>... dependsOn) {
        
        registerSystem(system);
        
        if (dependsOn != null && dependsOn.length > 0) {
            Set<Class<? extends GameSystem>> deps = new HashSet<>(Arrays.asList(dependsOn));
            dependencies.put(system.getClass(), deps);
            
            // Verify dependencies are registered
            for (Class<? extends GameSystem> dep : deps) {
                if (!systems.containsKey(dep)) {
                    System.err.println("SystemManager: Warning - dependency " + dep.getSimpleName() + 
                        " for " + system.getClass().getSimpleName() + " is not registered");
                }
            }
        }
    }
    
    /**
     * Unregister a system from the manager
     * 
     * @param <T> The type of system to unregister
     * @param systemClass The class of the system to unregister
     */
    public <T extends GameSystem> void unregisterSystem(Class<T> systemClass) {
        GameSystem system = systems.remove(systemClass);
        if (system != null) {
            updateOrder.remove(system);
            system.cleanup();
            systemProfiles.remove(systemClass);
            dependencies.remove(systemClass);
            
            // Publish system unregistration event
            if (initialized) {
                eventBus.publish(new SystemEvent(systemClass.getName(), SystemEvent.SystemEventType.UNREGISTERED));
            }
            
            System.out.println("SystemManager: Unregistered " + systemClass.getSimpleName());
        }
    }
    
    /**
     * Get a registered system by its class
     * 
     * @param <T> The type of system to get
     * @param systemClass The class of the system to get
     * @return The system instance, or null if not registered
     */
    @SuppressWarnings("unchecked")
    public <T extends GameSystem> T getSystem(Class<T> systemClass) {
        return (T) systems.get(systemClass);
    }
    
    /**
     * Check if a system is registered
     * 
     * @param systemClass The class of the system to check
     * @return True if the system is registered, false otherwise
     */
    public boolean hasSystem(Class<? extends GameSystem> systemClass) {
        return systems.containsKey(systemClass);
    }
    
    /**
     * Initialize all registered systems
     * Systems are initialized in dependency order
     */
    public void initialize() {
        if (initialized) {
            System.out.println("SystemManager: Already initialized");
            return;
        }
        
        System.out.println("SystemManager: Initializing " + systems.size() + " systems");
        
        // Sort systems by dependencies
        List<GameSystem> initOrder = getInitializationOrder();
        
        // Initialize systems in order
        for (GameSystem system : initOrder) {
            try {
                system.initialize();
                
                // Publish system initialization event
                eventBus.publish(new SystemEvent(system.getClass().getName(), 
                    SystemEvent.SystemEventType.INITIALIZED));
                
                System.out.println("SystemManager: Initialized " + system.getClass().getSimpleName());
            } catch (Exception e) {
                System.err.println("SystemManager: Failed to initialize " + 
                    system.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        initialized = true;
        
        // Publish system manager initialization event
        eventBus.publish(new SystemEvent("SystemManager", SystemEvent.SystemEventType.INITIALIZED));
        
        System.out.println("SystemManager: Initialization complete");
    }
    
    /**
     * Get the initialization order for systems based on dependencies
     * 
     * @return A list of systems in initialization order
     */
    private List<GameSystem> getInitializationOrder() {
        List<GameSystem> result = new ArrayList<>();
        Set<Class<? extends GameSystem>> visited = new HashSet<>();
        Set<Class<? extends GameSystem>> inProgress = new HashSet<>();
        
        // Perform topological sort based on dependencies
        for (Class<? extends GameSystem> systemClass : systems.keySet()) {
            if (!visited.contains(systemClass)) {
                visitSystem(systemClass, visited, inProgress, result);
            }
        }
        
        return result;
    }
    
    private void visitSystem(
            Class<? extends GameSystem> systemClass, 
            Set<Class<? extends GameSystem>> visited,
            Set<Class<? extends GameSystem>> inProgress,
            List<GameSystem> result) {
        
        // Check for circular dependencies
        if (inProgress.contains(systemClass)) {
            System.err.println("SystemManager: Warning - circular dependency detected for " + 
                systemClass.getSimpleName());
            return;
        }
        
        // Skip if already visited
        if (visited.contains(systemClass)) {
            return;
        }
        
        inProgress.add(systemClass);
        
        // Visit dependencies first
        Set<Class<? extends GameSystem>> deps = dependencies.get(systemClass);
        if (deps != null) {
            for (Class<? extends GameSystem> dep : deps) {
                if (systems.containsKey(dep) && !visited.contains(dep)) {
                    visitSystem(dep, visited, inProgress, result);
                }
            }
        }
        
        // Add this system to the result
        visited.add(systemClass);
        inProgress.remove(systemClass);
        result.add(systems.get(systemClass));
    }
    
    /**
     * Update all enabled systems in priority order
     * 
     * @param deltaTime The time elapsed since the last update in seconds
     */
    public void update(float deltaTime) {
        if (!initialized) {
            System.err.println("SystemManager: Cannot update - not initialized");
            return;
        }
        
        // Publish pre-update event
        eventBus.publish(new SystemEvent("SystemManager", SystemEvent.SystemEventType.PRE_UPDATE));
        
        // Update systems in priority order
        for (GameSystem system : updateOrder) {
            if (system.isEnabled()) {
                updateSystem(system, deltaTime);
            }
        }
        
        // Process events after all systems have updated
        eventBus.processEvents();
        
        // Publish post-update event
        eventBus.publish(new SystemEvent("SystemManager", SystemEvent.SystemEventType.POST_UPDATE));
    }
    
    /**
     * Update a single system with profiling if enabled
     * 
     * @param system The system to update
     * @param deltaTime The time elapsed since the last update in seconds
     */
    private void updateSystem(GameSystem system, float deltaTime) {
        if (enableSystemProfiling) {
            SystemProfile profile = systemProfiles.get(system.getClass());
            if (profile != null) {
                long startTime = System.nanoTime();
                
                try {
                    // Publish pre-system-update event
                    eventBus.publishImmediate(new SystemEvent(system.getClass().getName(), 
                        SystemEvent.SystemEventType.PRE_SYSTEM_UPDATE));
                    
                    // Update the system
                    system.update(deltaTime);
                    
                    // Publish post-system-update event
                    eventBus.publishImmediate(new SystemEvent(system.getClass().getName(), 
                        SystemEvent.SystemEventType.POST_SYSTEM_UPDATE));
                    
                    // Record profiling data
                    profile.recordUpdate(System.nanoTime() - startTime);
                } catch (Exception e) {
                    profile.recordError();
                    System.err.println("SystemManager: Error updating " + 
                        system.getClass().getSimpleName() + ": " + e.getMessage());
                    e.printStackTrace();
                    
                    // Publish system error event
                    eventBus.publishImmediate(new SystemEvent(system.getClass().getName(), 
                        SystemEvent.SystemEventType.ERROR));
                }
            }
        } else {
            try {
                // Update the system without profiling
                system.update(deltaTime);
            } catch (Exception e) {
                System.err.println("SystemManager: Error updating " + 
                    system.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace();
                
                // Publish system error event
                eventBus.publishImmediate(new SystemEvent(system.getClass().getName(), 
                    SystemEvent.SystemEventType.ERROR));
            }
        }
    }
    
    /**
     * Cleanup all systems
     * Systems are cleaned up in reverse initialization order
     */
    public void cleanup() {
        System.out.println("SystemManager: Cleaning up " + systems.size() + " systems");
        
        // Publish pre-cleanup event
        eventBus.publishImmediate(new SystemEvent("SystemManager", SystemEvent.SystemEventType.PRE_CLEANUP));
        
        // Cleanup in reverse order
        List<GameSystem> reverseOrder = new ArrayList<>(updateOrder);
        Collections.reverse(reverseOrder);
        
        for (GameSystem system : reverseOrder) {
            try {
                // Publish pre-system-cleanup event
                eventBus.publishImmediate(new SystemEvent(system.getClass().getName(), 
                    SystemEvent.SystemEventType.PRE_SYSTEM_CLEANUP));
                
                // Cleanup the system
                system.cleanup();
                
                // Publish post-system-cleanup event
                eventBus.publishImmediate(new SystemEvent(system.getClass().getName(), 
                    SystemEvent.SystemEventType.POST_SYSTEM_CLEANUP));
                
                System.out.println("SystemManager: Cleaned up " + system.getClass().getSimpleName());
            } catch (Exception e) {
                System.err.println("SystemManager: Error cleaning up " + 
                    system.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        systems.clear();
        updateOrder.clear();
        systemProfiles.clear();
        dependencies.clear();
        initialized = false;
        
        // Publish post-cleanup event
        eventBus.publishImmediate(new SystemEvent("SystemManager", SystemEvent.SystemEventType.POST_CLEANUP));
        
        System.out.println("SystemManager: Cleanup complete");
    }
    
    /**
     * Enable or disable a specific system
     * 
     * @param systemClass The class of the system to enable/disable
     * @param enabled True to enable, false to disable
     */
    public void setSystemEnabled(Class<? extends GameSystem> systemClass, boolean enabled) {
        GameSystem system = systems.get(systemClass);
        if (system != null) {
            system.setEnabled(enabled);
            
            // Publish system enabled/disabled event
            eventBus.publish(new SystemEvent(systemClass.getName(), 
                enabled ? SystemEvent.SystemEventType.ENABLED : SystemEvent.SystemEventType.DISABLED));
            
            System.out.println("SystemManager: " + systemClass.getSimpleName() + 
                " " + (enabled ? "enabled" : "disabled"));
        }
    }
    
    /**
     * Get all registered systems
     * 
     * @return A collection of all registered systems
     */
    public Collection<GameSystem> getAllSystems() {
        return new ArrayList<>(systems.values());
    }
    
    /**
     * Get systems in update order
     * 
     * @return A list of systems in update order
     */
    public List<GameSystem> getSystemsInUpdateOrder() {
        return new ArrayList<>(updateOrder);
    }
    
    /**
     * Enable or disable system profiling
     * 
     * @param enabled True to enable profiling, false to disable
     */
    public void setProfilingEnabled(boolean enabled) {
        this.enableSystemProfiling = enabled;
        
        if (enabled && systemProfiles.isEmpty()) {
            // Create profiles for existing systems
            for (GameSystem system : systems.values()) {
                systemProfiles.put(system.getClass(), 
                    new SystemProfile(system.getClass().getSimpleName()));
            }
        } else if (!enabled) {
            systemProfiles.clear();
        }
    }
    
    /**
     * Get profiling data for all systems
     * 
     * @return A map of system names to profiling data
     */
    public Map<String, SystemProfile> getProfilingData() {
        Map<String, SystemProfile> result = new HashMap<>();
        for (Map.Entry<Class<? extends GameSystem>, SystemProfile> entry : systemProfiles.entrySet()) {
            result.put(entry.getValue().getSystemName(), entry.getValue());
        }
        return result;
    }
    
    /**
     * Print system performance report
     */
    public void printPerformanceReport() {
        if (!enableSystemProfiling) {
            System.out.println("SystemManager: Profiling is not enabled");
            return;
        }
        
        System.out.println("=== System Performance Report ===");
        
        List<SystemProfile> profiles = new ArrayList<>(systemProfiles.values());
        profiles.sort((a, b) -> Double.compare(b.getAverageUpdateTime(), a.getAverageUpdateTime()));
        
        for (SystemProfile profile : profiles) {
            System.out.printf("%-20s: Avg: %.3fms, Max: %.3fms, Updates: %d, Errors: %d%n",
                profile.getSystemName(),
                profile.getAverageUpdateTime() / 1_000_000.0,
                profile.getMaxUpdateTime() / 1_000_000.0,
                profile.getUpdateCount(),
                profile.getErrorCount());
        }
        
        System.out.println("================================");
    }
    
    /**
     * Insert a system into the update order based on its priority
     * 
     * @param system The system to insert
     */
    private void insertSystemInOrder(GameSystem system) {
        int priority = system.getPriority();
        int insertIndex = 0;
        
        // Find correct position based on priority
        for (int i = 0; i < updateOrder.size(); i++) {
            if (updateOrder.get(i).getPriority() > priority) {
                insertIndex = i;
                break;
            }
            insertIndex = i + 1;
        }
        
        updateOrder.add(insertIndex, system);
    }
    
    /**
     * Helper class for system profiling
     */
    public static class SystemProfile {
        private final String systemName;
        private long totalUpdateTime = 0;
        private long maxUpdateTime = 0;
        private int updateCount = 0;
        private int errorCount = 0;
        
        public SystemProfile(String systemName) {
            this.systemName = systemName;
        }
        
        public void recordUpdate(long updateTimeNanos) {
            totalUpdateTime += updateTimeNanos;
            maxUpdateTime = Math.max(maxUpdateTime, updateTimeNanos);
            updateCount++;
        }
        
        public void recordError() {
            errorCount++;
        }
        
        public String getSystemName() { return systemName; }
        public double getAverageUpdateTime() { 
            return updateCount > 0 ? (double) totalUpdateTime / updateCount : 0; 
        }
        public long getMaxUpdateTime() { return maxUpdateTime; }
        public int getUpdateCount() { return updateCount; }
        public int getErrorCount() { return errorCount; }
        
        public void reset() {
            totalUpdateTime = 0;
            maxUpdateTime = 0;
            updateCount = 0;
            errorCount = 0;
        }
    }
}