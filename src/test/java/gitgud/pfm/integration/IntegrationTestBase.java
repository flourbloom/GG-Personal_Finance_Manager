package gitgud.pfm.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Base class for integration tests that require database access.
 * Uses an in-memory H2 database for testing.
 */
public abstract class IntegrationTestBase {

    protected Connection testConnection;

    @BeforeEach
    void setUpDatabase() throws SQLException {
        // Create in-memory H2 database for testing
        testConnection = DriverManager.getConnection(
            "jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1",
            "sa",
            ""
        );
        
        // Initialize database schema
        initializeTestSchema();
    }

    @AfterEach
    void tearDownDatabase() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            // Drop all tables
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute("DROP ALL OBJECTS");
            }
            testConnection.close();
        }
    }

    /**
     * Initialize the test database schema with all required tables
     */
    protected void initializeTestSchema() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            // Create categories table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    description TEXT,
                    type VARCHAR(20) NOT NULL
                )
            """);

            // Create transaction_records table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transaction_records (
                    id VARCHAR(50) PRIMARY KEY,
                    categoryId VARCHAR(50),
                    amount DECIMAL(15, 2) NOT NULL,
                    name VARCHAR(200),
                    income DECIMAL(1, 0),
                    walletId VARCHAR(50),
                    createTime VARCHAR(50),
                    FOREIGN KEY (categoryId) REFERENCES categories(id)
                )
            """);

            // Create Budget table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Budget (
                    id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    limitAmount DECIMAL(15, 2) NOT NULL,
                    balance DECIMAL(15, 2) NOT NULL,
                    startDate VARCHAR(50),
                    endDate VARCHAR(50)
                )
            """);

            // Create Goal table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Goal (
                    id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    target DECIMAL(15, 2) NOT NULL,
                    balance DECIMAL(15, 2) NOT NULL,
                    deadline VARCHAR(50),
                    priority DECIMAL(3, 1),
                    createAt VARCHAR(50)
                )
            """);

            // Create Wallet table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Wallet (
                    id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    balance DECIMAL(15, 2) NOT NULL,
                    color VARCHAR(50)
                )
            """);
        }
    }

    /**
     * Clear all data from test database tables
     */
    protected void clearAllTables() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM transaction_records");
            stmt.execute("DELETE FROM Budget");
            stmt.execute("DELETE FROM Goal");
            stmt.execute("DELETE FROM Wallet");
            stmt.execute("DELETE FROM categories");
        }
    }
}
