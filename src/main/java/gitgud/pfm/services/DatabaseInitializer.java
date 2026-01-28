package gitgud.pfm.services;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * Database Initializer - Ensures required tables exist
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Creates all required tables if they don't already exist. This is called
 * automatically when the Database singleton is initialized.
 * 
 * Tables created:
 * - Budget: Financial budget tracking with category limits
 * - Goal: Savings/financial goals with deadlines
 * - Wallets: Account/wallet management (renamed from Accounts conceptually)
 * - transaction_records: Individual transaction records
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
public class DatabaseInitializer {

    /**
     * Initialize the database by creating all required tables if they don't exist.
     * Called automatically on first Database connection.
     *
     * @param connection The active database connection
     * @throws SQLException If table creation fails
     */
    public static void initializeDatabase(Connection connection) throws SQLException {
        if (connection == null) {
            throw new SQLException("Cannot initialize database: connection is null");
        }

        try (Statement statement = connection.createStatement()) {
            // Create Account table (must be first for foreign key references)
            if (!tableExists(connection, "Wallet")) {
                String createAccountSQL = """
                    CREATE TABLE "Wallet" (
                        "id"  TEXT NOT NULL,
                        "name"  TEXT,
                        "balance"  NUMERIC,
                        "color"  TEXT,
                        PRIMARY KEY("id")
                    )
                    """;
                statement.execute(createAccountSQL);
                System.out.println("✓ Created table: Wallet");
            }

            // Create Category table (referenced by transactions and budgets)
            if (!tableExists(connection, "Category")) {
                String createCategorySQL = """
                    CREATE TABLE "Category" (
                        "id"  TEXT NOT NULL,
                        "name"  TEXT NOT NULL,
                        "description"  TEXT,
                        "color"  TEXT,
                        PRIMARY KEY("id")
                    )
                    """;
                statement.execute(createCategorySQL);
                System.out.println("✓ Created table: Category");
            }

            // Create Budget table (links to categories via junction table)
            if (!tableExists(connection, "Budget")) {
                String createBudgetSQL = """
                    CREATE TABLE "Budget" (
                        "id"  TEXT NOT NULL,
                        "name"  TEXT,
                        "limitAmount"  NUMERIC,
                        "balance"  NUMERIC,
                        "startDate"  TEXT,
                        "endDate"  TEXT,
                        PRIMARY KEY("id")
                    )
                    """;
                statement.execute(createBudgetSQL);
                System.out.println("✓ Created table: Budget");
            }

            // Create Goal table (links to wallet for funding)
            if (!tableExists(connection, "Goal")) {
                String createGoalSQL = """
                    CREATE TABLE "Goal" (
                        "id"  TEXT NOT NULL,
                        "name"  TEXT,
                        "target"  NUMERIC,
                        "balance"  NUMERIC,
                        "deadline"  TEXT,
                        "priority"  NUMERIC,
                        "createAt"  TEXT,
                        "walletId"  TEXT,
                        PRIMARY KEY("id"),
                        FOREIGN KEY("walletId") REFERENCES "Wallet"("id") ON DELETE SET NULL
                    )
                    """;
                statement.execute(createGoalSQL);
                System.out.println("✓ Created table: Goal");
            }

            // Create transaction_records table with proper foreign key to Category
            if (!tableExists(connection, "transaction_records")) {
                String createTransactionSQL = """
                    CREATE TABLE "transaction_records" (
                        "id"  TEXT NOT NULL,
                        "categoryId"  TEXT,
                        "amount"  NUMERIC,
                        "name"  TEXT,
                        "income"  NUMERIC,
                        "walletId"  TEXT,
                        "createTime"  TEXT,
                        PRIMARY KEY("id"),
                        FOREIGN KEY("walletId") REFERENCES "Wallet"("id") ON DELETE CASCADE,
                        FOREIGN KEY("categoryId") REFERENCES "Category"("id") ON DELETE SET NULL
                    )
                    """;
                statement.execute(createTransactionSQL);
                System.out.println("✓ Created table: transaction_records");
            }

            // Create Budget_Category junction table (many-to-many relationship)
            if (!tableExists(connection, "Budget_Category")) {
                String createBudgetCategorySQL = """
                    CREATE TABLE "Budget_Category" (
                        "budgetID"  TEXT NOT NULL,
                        "categoryID"  TEXT NOT NULL,
                        PRIMARY KEY("budgetID", "categoryID"),
                        FOREIGN KEY("budgetID") REFERENCES "Budget"("id") ON DELETE CASCADE,
                        FOREIGN KEY("categoryID") REFERENCES "Category"("id") ON DELETE CASCADE
                    )
                    """;
                statement.execute(createBudgetCategorySQL);
                System.out.println("✓ Created table: Budget_Category (junction)");
            }

            System.out.println("Database initialization complete with proper foreign key relationships.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Check if a table exists in the database.
     *
     * @param connection The database connection
     * @param tableName  The name of the table to check
     * @return true if the table exists, false otherwise
     * @throws SQLException If the check fails
     */
    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return tables.next();
        }
    }

    /**
     * Drop all tables in the database (useful for testing/reset).
     * WARNING: This is destructive and will remove all data.
     *
     * @param connection The database connection
     * @throws SQLException If the operation fails
     */
    public static void dropAllTables(Connection connection) throws SQLException {
        if (connection == null) {
            throw new SQLException("Cannot drop tables: connection is null");
        }

        // Drop in reverse order of dependencies (junction tables first)
        String[] tableNames = {"Budget_Category", "Goal_Category", "transaction_records", "Budget", "Goal", "Wallet", "Category"};

        try (Statement statement = connection.createStatement()) {
            for (String tableName : tableNames) {
                if (tableExists(connection, tableName)) {
                    statement.execute("DROP TABLE \"" + tableName + "\"");
                    System.out.println("✓ Dropped table: " + tableName);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error dropping tables: " + e.getMessage());
            throw e;
        }
    }
}
