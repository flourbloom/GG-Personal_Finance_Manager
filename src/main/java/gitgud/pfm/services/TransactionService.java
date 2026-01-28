package gitgud.pfm.services;

import gitgud.pfm.Models.Transaction;
import gitgud.pfm.interfaces.CRUDInterface;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionService - Explicit CRUD operations for Transaction entity
 * All SQL queries explicitly show field mappings for clarity
 */
public class TransactionService implements CRUDInterface<Transaction> {
    private final Connection connection;
    
    public TransactionService() {
        this.connection = Database.getInstance().getConnection();
    }
    
    /**
     * Create a new transaction in the database
     * Explicit fields: ID, Categories, Amount, Name, Income, AccountID, Create_time
     */
    @Override
    public void create(Transaction transaction) {
        String sql = "INSERT INTO transaction_records (ID, Categories, Amount, Name, Income, AccountID, Create_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getID());
            pstmt.setString(2, transaction.getCategories());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getName());
            pstmt.setDouble(5, transaction.getIncome());
            pstmt.setString(6, transaction.getAccountID());
            pstmt.setString(7, transaction.getCreate_time());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating transaction: " + e.getMessage());
        }
    }
    
    /**
     * Read a single transaction by ID
     * Explicit fields: ID, Categories, Amount, Name, Income, AccountID, Create_time
     */
    @Override
    public Transaction read(String id) {
        String sql = "SELECT ID, Categories, Amount, Name, Income, AccountID, Create_time " +
                     "FROM transaction_records WHERE ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Transaction transaction = new Transaction();
                    transaction.setID(rs.getString("ID"));
                    transaction.setCategories(rs.getString("Categories"));
                    transaction.setAmount(rs.getDouble("Amount"));
                    transaction.setName(rs.getString("Name"));
                    transaction.setIncome(rs.getDouble("Income"));
                    transaction.setAccountID(rs.getString("AccountID"));
                    transaction.setCreate_time(rs.getString("Create_time"));
                    return transaction;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading transaction: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Read all transactions from the database
     * Explicit fields: ID, Categories, Amount, Name, Income, AccountID, Create_time
     */
    public List<Transaction> readAll() {
        String sql = "SELECT ID, Categories, Amount, Name, Income, AccountID, Create_time " +
                     "FROM transaction_records ORDER BY Create_time DESC";
        List<Transaction> transactions = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setID(rs.getString("ID"));
                transaction.setCategories(rs.getString("Categories"));
                transaction.setAmount(rs.getDouble("Amount"));
                transaction.setName(rs.getString("Name"));
                transaction.setIncome(rs.getDouble("Income"));
                transaction.setAccountID(rs.getString("AccountID"));
                transaction.setCreate_time(rs.getString("Create_time"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all transactions: " + e.getMessage());
        }
        return transactions;
    }
    
    /**
     * Read all transactions by account ID
     * Explicit fields: ID, Categories, Amount, Name, Income, AccountID, Create_time
     */
    public List<Transaction> readByAccount(String accountID) {
        String sql = "SELECT ID, Categories, Amount, Name, Income, AccountID, Create_time " +
                     "FROM transaction_records WHERE AccountID = ? ORDER BY Create_time DESC";
        List<Transaction> transactions = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction();
                    transaction.setID(rs.getString("ID"));
                    transaction.setCategories(rs.getString("Categories"));
                    transaction.setAmount(rs.getDouble("Amount"));
                    transaction.setName(rs.getString("Name"));
                    transaction.setIncome(rs.getDouble("Income"));
                    transaction.setAccountID(rs.getString("AccountID"));
                    transaction.setCreate_time(rs.getString("Create_time"));
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading transactions by account: " + e.getMessage());
        }
        return transactions;
    }
    
    /**
     * Update an existing transaction
     * Explicit fields: Categories, Amount, Name, Income, AccountID, Create_time (WHERE ID = ?)
     */
    @Override
    public void update(Transaction transaction) {
        String sql = "UPDATE transaction_records SET Categories = ?, Amount = ?, Name = ?, " +
                     "Income = ?, AccountID = ?, Create_time = ? WHERE ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getCategories());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getName());
            pstmt.setDouble(4, transaction.getIncome());
            pstmt.setString(5, transaction.getAccountID());
            pstmt.setString(6, transaction.getCreate_time());
            pstmt.setString(7, transaction.getID());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
        }
    }
    
    /**
     * Delete a transaction by ID
     */
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM transaction_records WHERE ID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
        }
    }
    
    /**
     * Get total income across all transactions
     */
    public double getTotalIncome() {
        String sql = "SELECT SUM(Amount) as total FROM transaction_records WHERE Income = 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total income: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * Get total expenses across all transactions
     */
    public double getTotalExpenses() {
        String sql = "SELECT SUM(Amount) as total FROM transaction_records WHERE Income = 0";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total expenses: " + e.getMessage());
        }
        return 0.0;
    }
}