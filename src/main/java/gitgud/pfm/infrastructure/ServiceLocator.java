package gitgud.pfm.infrastructure;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Simple Dependency Injection Container / Service Locator
 * Manages service instances and dependencies
 * 
 * Usage:
 * - Register services at application startup
 * - Retrieve services when needed
 * 
 * Example:
 *   ServiceLocator.register(TransactionService.class, new TransactionService());
 *   TransactionService service = ServiceLocator.get(TransactionService.class);
 */
public class ServiceLocator {
    private static ServiceLocator instance;
    private final Map<Class<?>, Object> services = new HashMap<>();
    private final Map<Class<?>, Supplier<?>> factories = new HashMap<>();
    
    private ServiceLocator() {
        // Private constructor for singleton
    }
    
    public static ServiceLocator getInstance() {
        if (instance == null) {
            synchronized (ServiceLocator.class) {
                if (instance == null) {
                    instance = new ServiceLocator();
                }
            }
        }
        return instance;
    }
    
    /**
     * Register a singleton service instance
     * @param serviceClass The interface or class type
     * @param implementation The concrete implementation
     */
    public static <T> void register(Class<T> serviceClass, T implementation) {
        getInstance().services.put(serviceClass, implementation);
    }
    
    /**
     * Register a factory for lazy initialization
     * @param serviceClass The interface or class type
     * @param factory Supplier that creates the instance
     */
    public static <T> void registerFactory(Class<T> serviceClass, Supplier<T> factory) {
        getInstance().factories.put(serviceClass, factory);
    }
    
    /**
     * Get a service instance
     * @param serviceClass The service class to retrieve
     * @return The service instance
     * @throws IllegalStateException if service not registered
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> serviceClass) {
        ServiceLocator locator = getInstance();
        
        // Check if already instantiated
        Object service = locator.services.get(serviceClass);
        if (service != null) {
            return (T) service;
        }
        
        // Check if factory exists
        Supplier<?> factory = locator.factories.get(serviceClass);
        if (factory != null) {
            service = factory.get();
            locator.services.put(serviceClass, service);
            return (T) service;
        }
        
        throw new IllegalStateException(
            "Service not registered: " + serviceClass.getName() + 
            ". Make sure to register it in ApplicationContext.initialize()"
        );
    }
    
    /**
     * Check if a service is registered
     */
    public static boolean isRegistered(Class<?> serviceClass) {
        ServiceLocator locator = getInstance();
        return locator.services.containsKey(serviceClass) || 
               locator.factories.containsKey(serviceClass);
    }
    
    /**
     * Clear all registered services (useful for testing)
     */
    public static void clear() {
        getInstance().services.clear();
        getInstance().factories.clear();
    }
    
    /**
     * Reset the singleton instance (useful for testing)
     */
    public static void reset() {
        instance = null;
    }
}
