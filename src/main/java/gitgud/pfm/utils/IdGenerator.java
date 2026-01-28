package gitgud.pfm.utils;

import java.util.UUID;

/**
 * IdGenerator - Utility class for generating unique IDs for financial entities
 */
public class IdGenerator {
    
    /**
     * Generate a unique ID for Account
     * Format: ACC_{timestamp}_{randomUUID}
     */
    public static String generateAccountId() {
        return "ACC_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Generate a unique ID for Budget
     * Format: BUD_{timestamp}_{randomUUID}
     */
    public static String generateBudgetId() {
        return "BUD_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Generate a unique ID for Goal
     * Format: GOL_{timestamp}_{randomUUID}
     */
    public static String generateGoalId() {
        return "GOL_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Generate a unique ID for Transaction
     * Format: TXN_{timestamp}_{randomUUID}
     */
    public static String generateTransactionId() {
        return "TXN_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
