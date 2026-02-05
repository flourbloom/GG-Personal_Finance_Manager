package gitgud.pfm.infrastructure;

import gitgud.pfm.services.*;
import gitgud.pfm.services.business.*;
import gitgud.pfm.services.navigation.NavigationService;
import gitgud.pfm.viewmodels.*;
import java.sql.Connection;

/**
 * Application Context - Initializes and registers all services
 * This is called once at application startup
 */
public class ApplicationContext {
    
    private static boolean initialized = false;
    
    /**
     * Initialize all services and register them with ServiceLocator
     * Call this method once at application startup
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        System.out.println("Initializing Application Context...");
        
        // Infrastructure Services
        registerInfrastructure();
        
        // Data Services
        registerDataServices();
        
        // Business Services
        registerBusinessServices();
        
        // Navigation Service
        registerNavigationService();
        
        // View Models
        registerViewModels();
        
        initialized = true;
        System.out.println("Application Context initialized successfully.");
    }
    
    private static void registerInfrastructure() {
        // Database connection (singleton)
        Connection connection = Database.getInstance().getConnection();
        ServiceLocator.register(Connection.class, connection);
    }
    
    private static void registerDataServices() {
        // Register data access services as singletons
        ServiceLocator.registerFactory(TransactionService.class, TransactionService::new);
        ServiceLocator.registerFactory(GoalService.class, GoalService::new);
        ServiceLocator.registerFactory(BudgetService.class, BudgetService::new);
        ServiceLocator.registerFactory(WalletService.class, WalletService::new);
        ServiceLocator.registerFactory(CategoryService.class, CategoryService::new);
    }
    
    private static void registerBusinessServices() {
        // Register business logic services
        ServiceLocator.registerFactory(BudgetCalculationService.class, 
            () -> new BudgetCalculationService(
                ServiceLocator.get(BudgetService.class),
                ServiceLocator.get(TransactionService.class)
            )
        );
        
        ServiceLocator.registerFactory(GoalProgressService.class,
            () -> new GoalProgressService(
                ServiceLocator.get(GoalService.class),
                ServiceLocator.get(TransactionService.class)
            )
        );
        
        ServiceLocator.registerFactory(TransactionFilterService.class,
            () -> new TransactionFilterService()
        );
    }
    
    private static void registerNavigationService() {
        // Navigation service (singleton, will be set by App.java)
        ServiceLocator.registerFactory(NavigationService.class, NavigationService::new);
    }
    
    private static void registerViewModels() {
        // ViewModels - created fresh for each controller
        // Note: These are registered as factories, not singletons
        ServiceLocator.registerFactory(DashboardViewModel.class,
            () -> new DashboardViewModel(
                ServiceLocator.get(BudgetCalculationService.class),
                ServiceLocator.get(GoalProgressService.class),
                ServiceLocator.get(TransactionService.class)
            )
        );
    }
    
    /**
     * Check if context is initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Reset context (for testing)
     */
    public static void reset() {
        initialized = false;
        ServiceLocator.reset();
    }
}
