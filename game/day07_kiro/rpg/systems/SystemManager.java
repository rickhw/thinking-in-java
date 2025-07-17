package rpg.systems;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for game systems that handles registration, initialization, and execution order.
 * Provides centralized control over all game systems and their lifecycle.
 */
public class SystemManager {
    private final Map<Class<? extends GameSystem>, GameSystem> systems;
    private final List<GameSystem> updateOrder;
    private final EventBus eventBus;
    
    // System management settings
    private boolean initialized = false;
    private boolean enableSystemProfiling = false;
    private Map<Class<? extends GameSystem>, SystemProfile> systemProfiles;
    
    public SystemManager(EventBus eventBus) {
        this.systems = new ConcurrentHashMap<>();
        this.updateOrder = new ArrayList<>();
        this.eventBus = eventBus;
        this.systemProfiles = new HashMap<>();
    }
    
    /**
     * Register a system with the manager
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
        }
        
        // Create profiling data if enabled
        if (enableSystemProfiling) {
            systemProfiles.put(systemClass, new SystemProfile(systemClass.getSimpleName()));
        }
        
        System.out.println("SystemManager: Registered " + systemClass.getSimpleName());
    }
    
    /**
     * Unregister a system from the manager
     */
    public <T extends GameSystem> void unregisterSystem(Class<T> systemClass) {
        GameSystem system = systems.remove(systemClass);
        if (system != null) {
            updateOrder.remove(system);
            system.cleanup();
            systemProfiles.remove(systemClass);
            System.out.println("SystemManager: Unregistered " + systemClass.getSimpleName());
        }
    }
    
    /**
     * Get a registered system by its class
     */
    @SuppressWarnings("unchecked")
    public <T extends GameSystem> T getSystem(Class<T> systemClass) {
        return (T) systems.get(systemClass);
    }
    
    /**
     * Check if a system is registered
     */
    public boolean hasSystem(Class<? extends GameSystem> systemClass) {
        return systems.containsKey(systemClass);
    }
    
    /**
     * Initialize all registered systems
     */
    public void initialize() {
        if (initialized) {
            System.out.println("SystemManager: Already initialized");
            return;
        }
        
        System.out.println("SystemManager: Initializing " + systems.size() + " systems");
        
        for (GameSystem system : updateOrder) {
            try {
                system.initialize();
                System.out.println("SystemManager: Initialized " + system.getClass().getSimpleName());
            } catch (Exception e) {
                System.err.println("SystemManager: Failed to initialize " + 
                    system.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        initialized = true;
        System.out.println("SystemManager: Initialization complete");
    }
    
    /**
     * Update all enabled systems in priority order
     */
    public void update(float deltaTime) {
        if (!initialized) {
            System.err.println("SystemManager: Cannot update - not initialized");
            return;
        }
        
        for (GameSystem system : updateOrder) {
            if (system.isEnabled()) {
                updateSystem(system, deltaTime);
            }
        }
        
        // Process events after all systems have updated
        eventBus.processEvents();
    }
    
    private void updateSystem(GameSystem system, float deltaTime) {
        if (enableSystemProfiling) {
            SystemProfile profile = systemProfiles.get(system.getClass());
            if (profile != null) {
                long startTime = System.nanoTime();
                
                try {
                    system.update(deltaTime);
                    profile.recordUpdate(System.nanoTime() - startTime);
                } catch (Exception e) {
                    profile.recordError();
                    System.err.println("SystemManager: Error updating " + 
                        system.getClass().getSimpleName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            try {
                system.update(deltaTime);
            } catch (Exception e) {
                System.err.println("SystemManager: Error updating " + 
                    system.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Cleanup all systems
     */
    public void cleanup() {
        System.out.println("SystemManager: Cleaning up " + systems.size() + " systems");
        
        // Cleanup in reverse order
        List<GameSystem> reverseOrder = new ArrayList<>(updateOrder);
        Collections.reverse(reverseOrder);
        
        for (GameSystem system : reverseOrder) {
            try {
                system.cleanup();
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
        initialized = false;
        
        System.out.println("SystemManager: Cleanup complete");
    }
    
    /**
     * Enable or disable a specific system
     */
    public void setSystemEnabled(Class<? extends GameSystem> systemClass, boolean enabled) {
        GameSystem system = systems.get(systemClass);
        if (system != null) {
            system.setEnabled(enabled);
            System.out.println("SystemManager: " + systemClass.getSimpleName() + 
                " " + (enabled ? "enabled" : "disabled"));
        }
    }
    
    /**
     * Get all registered systems
     */
    public Collection<GameSystem> getAllSystems() {
        return new ArrayList<>(systems.values());
    }
    
    /**
     * Get systems in update order
     */
    public List<GameSystem> getSystemsInUpdateOrder() {
        return new ArrayList<>(updateOrder);
    }
    
    /**
     * Enable or disable system profiling
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