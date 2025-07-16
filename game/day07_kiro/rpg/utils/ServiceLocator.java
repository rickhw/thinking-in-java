package rpg.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Service Locator pattern implementation for dependency injection.
 * Provides centralized access to game services and manages their lifecycle.
 */
public class ServiceLocator {
    private static final Map<Class<?>, Object> services = new HashMap<>();
    private static boolean initialized = false;
    
    /**
     * Register a service instance with the locator.
     */
    public static <T> void registerService(Class<T> serviceType, T serviceInstance) {
        if (serviceInstance == null) {
            throw new IllegalArgumentException("Service instance cannot be null");
        }
        services.put(serviceType, serviceInstance);
    }
    
    /**
     * Get a service instance by type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> serviceType) {
        T service = (T) services.get(serviceType);
        if (service == null) {
            throw new IllegalStateException("Service not registered: " + serviceType.getSimpleName());
        }
        return service;
    }
    
    /**
     * Check if a service is registered.
     */
    public static boolean hasService(Class<?> serviceType) {
        return services.containsKey(serviceType);
    }
    
    /**
     * Unregister a service.
     */
    public static void unregisterService(Class<?> serviceType) {
        services.remove(serviceType);
    }
    
    /**
     * Clear all registered services.
     */
    public static void clear() {
        services.clear();
        initialized = false;
    }
    
    /**
     * Initialize the service locator with default services.
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        // Register default services
        registerService(rpg.game.EntityManager.class, new rpg.game.EntityManager());
        
        initialized = true;
    }
    
    /**
     * Check if the service locator has been initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Get all registered service types.
     */
    public static Class<?>[] getRegisteredServiceTypes() {
        return services.keySet().toArray(new Class<?>[0]);
    }
}