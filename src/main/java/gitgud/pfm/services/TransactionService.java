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
     * Explicit fields: id, categoryId, amount, name, income, walletId, createTime
     */
    @Override
    public void create(Transaction transaction) {
        String sql = "INSERT INTO transaction_records (id, categoryId, amount, name, income, walletId, createTime) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getId());
            pstmt.setString(2, transaction.getCategoryId());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getName());
            pstmt.setDouble(5, transaction.getIncome());
            pstmt.setString(6, transaction.getWalletId());
            pstmt.setString(7, transaction.getCreateTime());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating transaction: " + e.getMessage());
        }
    }
    
    /**
     * Read a single transaction by ID
     * Explicit fields: id, categoryId, amount, name, income, walletId, createTime
     */
    @Override
    public Transaction read(String id) {
        String sql = "SELECT id, categoryId, amount, name, income, walletId, createTime " +
                 "FROM transaction_records WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Transaction transaction = new Transaction();
                    transaction.setId(rs.getString("id"));
                    transaction.setCategoryId(rs.getString("categoryId"));
                    transaction.setAmount(rs.getDouble("amount"));
                    transaction.setName(rs.getString("name"));
                    transaction.setIncome(rs.getDouble("income"));
                    transaction.setWalletId(rs.getString("walletId"));
                    transaction.setCreateTime(rs.getString("createTime"));
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
     * Explicit fields: id, categoryId, amount, name, income, walletId, createTime
     */
    public List<Transaction> readAll() {
        String sql = "SELECT id, categoryId, amount, name, income, walletId, createTime " +
             "FROM transaction_records ORDER BY createTime DESC";
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getString("id"));
                transaction.setCategoryId(rs.getString("categoryId"));
                transaction.setAmount(rs.getDouble("amount"));
                transaction.setName(rs.getString("name"));
                transaction.setIncome(rs.getDouble("income"));
                transaction.setWalletId(rs.getString("walletId"));
                transaction.setCreateTime(rs.getString("createTime"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all transactions: " + e.getMessage());
        }
        return transactions;
    }
    
    /**
     * Read all transactions by wallet ID
     * Explicit fields: id, categoryId, amount, name, income, walletId, createTime
     */
    public List<Transaction> readByWallet(String walletID) {
        String sql = "SELECT id, categoryId, amount, name, income, walletId, createTime " +
                 "FROM transaction_records WHERE walletId = ? ORDER BY createTime DESC";
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, walletID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction();
                    transaction.setId(rs.getString("id"));
                    transaction.setCategoryId(rs.getString("categoryId"));
                    transaction.setAmount(rs.getDouble("amount"));
                    transaction.setName(rs.getString("name"));
                    transaction.setIncome(rs.getDouble("income"));
                    transaction.setWalletId(rs.getString("walletId"));
                    transaction.setCreateTime(rs.getString("createTime"));
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading transactions by wallet: " + e.getMessage());
        }
        return transactions;
    }
    
    /**
     * Update an existing transaction
     * Explicit fields: categoryId, amount, name, income, walletId, createTime (WHERE id = ?)
     */
    @Override
    public void update(Transaction transaction) {
        String sql = "UPDATE transaction_records SET categoryId = ?, amount = ?, name = ?, " +
             "income = ?, walletId = ?, createTime = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getCategoryId());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getName());
            pstmt.setDouble(4, transaction.getIncome());
            pstmt.setString(5, transaction.getWalletId());
            pstmt.setString(6, transaction.getCreateTime());
            pstmt.setString(7, transaction.getId());
            
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
        String sql = "DELETE FROM transaction_records WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
        }
    }
    
    /**
     * Delete all transactions that belong to a specific wallet
     */
    public void deleteByWalletId(String walletId) {
        String sql = "DELETE FROM transaction_records WHERE walletId = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, walletId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting transactions for wallet: " + e.getMessage());
        }
    }
    
    /**
     * Get total income across all transactions
     */
    public double getTotalIncome() {
        String sql = "SELECT SUM(amount) as total FROM transaction_records WHERE income = 1";
        
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
        String sql = "SELECT SUM(amount) as total FROM transaction_records WHERE income = 0";
        
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
    public List<Transaction> findByName(String namePattern) {
		List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT id, categoryId, amount, name, income, walletId, createTime FROM transaction_records WHERE name LIKE ? ORDER BY name";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)){
            pstmt.setString(1, namePattern);
            try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Transaction transaction = new Transaction();
                    transaction.setId(rs.getString("id"));
                    transaction.setCategoryId(rs.getString("categoryId"));
                    transaction.setAmount(rs.getDouble("amount"));
                    transaction.setName(rs.getString("name"));
                    transaction.setIncome(rs.getDouble("income"));
                    transaction.setWalletId(rs.getString("walletId"));
                    transaction.setCreateTime(rs.getString("createTime"));
                    transactions.add(transaction);
            }
        }
        } catch (SQLException e) {
            System.out.println("Error searching transactions by name: " + e.getMessage());
        }
        return transactions;
    }
}