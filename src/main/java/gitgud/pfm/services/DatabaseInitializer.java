package gitgud.pfm.services;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
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
 * - Wallets: Wallet management (formerly Accounts)
 * - Account: serves a new purpose referring to the instance of the user program-wide
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
            // Create Wallet table (must be first for foreign key references)
            if (!tableExists(connection, "Wallet")) {
                String createWalletSQL = """
                    CREATE TABLE "Wallet" (
                        "id"  TEXT NOT NULL,
                        "name"  TEXT,
                        "balance"  NUMERIC,
                        "color"  TEXT,
                        PRIMARY KEY("id")
                    )
                    """;
                statement.execute(createWalletSQL);
                System.out.println("✓ Created table: Wallet");
            }

            // Create Category table (referenced by transactions and budgets)
            boolean categoryTableCreated = false;
            if (!tableExists(connection, "Category")) {
                String createCategorySQL = """
                    CREATE TABLE "Category" (
                        "id"  TEXT NOT NULL,
                        "name"  TEXT NOT NULL,
                        "description"  TEXT,
                        "type"  TEXT NOT NULL DEFAULT 'EXPENSE',
                        "color"  TEXT,
                        PRIMARY KEY("id")
                    )
                    """;
                statement.execute(createCategorySQL);
                System.out.println("✓ Created table: Category");
                categoryTableCreated = true;
            } else {
                // Add type column if it doesn't exist (for existing databases)
                addColumnIfNotExists(connection, "Category", "type", "TEXT NOT NULL DEFAULT 'EXPENSE'");
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
                        "periodType"  TEXT DEFAULT 'MONTHLY',
                        "walletId"  TEXT,
                        PRIMARY KEY("id"),
                        FOREIGN KEY("walletId") REFERENCES "Wallet"("id") ON DELETE SET NULL
                    )
                    """;
                statement.execute(createBudgetSQL);
                System.out.println("✓ Created table: Budget");
            } else {
                // Add new columns if table exists but columns are missing
                addColumnIfNotExists(connection, "Budget", "periodType", "TEXT DEFAULT 'MONTHLY'");
                addColumnIfNotExists(connection, "Budget", "walletId", "TEXT");
            }

            // Create Goal table (account-wide goals)
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
            } else {
                // Add walletId column if it doesn't exist (for existing databases)
                addColumnIfNotExists(connection, "Goal", "walletId", "TEXT");
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
                        "categoryLimit"  NUMERIC,
                        PRIMARY KEY("budgetID", "categoryID"),
                        FOREIGN KEY("budgetID") REFERENCES "Budget"("id") ON DELETE CASCADE,
                        FOREIGN KEY("categoryID") REFERENCES "Category"("id") ON DELETE CASCADE
                    )
                    """;
                statement.execute(createBudgetCategorySQL);
                System.out.println("✓ Created table: Budget_Category (junction)");
            } else {
                // Add categoryLimit column if it doesn't exist
                addColumnIfNotExists(connection, "Budget_Category", "categoryLimit", "NUMERIC");
            }

            // Seed default categories if the table was just created or is empty
            if (categoryTableCreated || isCategoryTableEmpty(connection)) {
                seedDefaultCategories(connection);
            }

            System.out.println("Database initialization complete with proper foreign key relationships.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Check if the Category table is empty
     */
    private static boolean isCategoryTableEmpty(Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Category";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return true;
    }

    /**
     * Seed default categories into the database
     * Categories are ordered by type (EXPENSE first, then INCOME) to match CLI display
     */
    private static void seedDefaultCategories(Connection connection) throws SQLException {
        String insertSQL = "INSERT OR IGNORE INTO Category (id, name, description, type) VALUES (?, ?, ?, ?)";
        
        // Default categories grouped by type to match display order
        Object[][] defaultCategories = {
            // EXPENSE categories first (IDs 1-9)
            {"1", "Food & Drinks", "Meals, groceries, and beverages", "EXPENSE"},
            {"2", "Transport", "Public transport, fuel, taxis, etc.", "EXPENSE"},
            {"3", "Home Bills", "Rent, electricity, water, gas, etc.", "EXPENSE"},
            {"4", "Self-care", "Personal care, beauty, spa, etc.", "EXPENSE"},
            {"5", "Shopping", "Clothes, gadgets, and other shopping", "EXPENSE"},
            {"6", "Health", "Medical, pharmacy, insurance", "EXPENSE"},
            {"7", "Subscription", "Streaming, software, memberships", "EXPENSE"},
            {"8", "Entertainment & Sport", "Movies, games, sports activities", "EXPENSE"},
            {"9", "Traveling", "Flights, hotels, vacation expenses", "EXPENSE"},
            // INCOME categories last (IDs 10-11)
            {"10", "Salary", "Monthly salary income", "INCOME"},
            {"11", "Investment", "Investment returns, dividends, etc.", "INCOME"}
        };

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            for (Object[] cat : defaultCategories) {
                pstmt.setString(1, (String) cat[0]);
                pstmt.setString(2, (String) cat[1]);
                pstmt.setString(3, (String) cat[2]);
                pstmt.setString(4, (String) cat[3]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("✓ Seeded " + defaultCategories.length + " default categories");
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
     * Check if a column exists in a table.
     *
     * @param connection The database connection
     * @param tableName  The name of the table
     * @param columnName The name of the column to check
     * @return true if the column exists, false otherwise
     * @throws SQLException If the check fails
     */
    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
            return columns.next();
        }
    }
    
    /**
     * Add a column to a table if it doesn't exist.
     *
     * @param connection The database connection
     * @param tableName  The name of the table
     * @param columnName The name of the column to add
     * @param columnDef  The column definition (e.g., "TEXT DEFAULT 'value'")
     * @throws SQLException If the operation fails
     */
    private static void addColumnIfNotExists(Connection connection, String tableName, 
                                            String columnName, String columnDef) throws SQLException {
        if (!columnExists(connection, tableName, columnName)) {
            String alterSQL = "ALTER TABLE \"" + tableName + "\" ADD COLUMN \"" + columnName + "\" " + columnDef;
            try (Statement statement = connection.createStatement()) {
                statement.execute(alterSQL);
                System.out.println("✓ Added column " + columnName + " to table " + tableName);
            }
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
