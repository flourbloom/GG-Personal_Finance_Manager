package gitgud.pfm.services;

import gitgud.pfm.Models.Account;
import gitgud.pfm.interfaces.CRUDInterface;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AccountService - Explicit CRUD operations for Account entity
 * All SQL queries explicitly show field mappings for clarity
 */
public class AccountService implements CRUDInterface<Account> {
    private final Connection connection;
    
    public AccountService() {
        this.connection = Database.getInstance().getConnection();
    }
    
    /**
     * Create a new account in the database
     * Explicit fields: accountID, name, balance, color
     */
    @Override
    public void create(Account account) {
        String sql = "INSERT INTO Account (accountID, name, balance, color) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, account.getId());
            pstmt.setString(2, account.getName());
            pstmt.setDouble(3, account.getBalance());
            pstmt.setString(4, account.getColor());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating account: " + e.getMessage());
        }
    }
    
    /**
     * Read a single account by accountID
     * Explicit fields: accountID, name, balance, color
     */
    @Override
    public Account read(String accountID) {
        String sql = "SELECT accountID, name, balance, color FROM Account WHERE accountID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Account account = new Account();
                    account.setId(rs.getString("accountID"));
                    account.setName(rs.getString("name"));
                    account.setBalance(rs.getDouble("balance"));
                    account.setColor(rs.getString("color"));
                    account.setId(rs.getString("accountID")); // Set parent class id
                    return account;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading account: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Read all accounts from the database
     * Explicit fields: accountID, name, balance, color
     */
    public List<Account> readAll() {
        String sql = "SELECT accountID, name, balance, color FROM Account ORDER BY name";
        List<Account> accounts = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Account account = new Account();
                account.setId(rs.getString("accountID"));
                account.setName(rs.getString("name"));
                account.setBalance(rs.getDouble("balance"));
                account.setColor(rs.getString("color"));
                account.setId(rs.getString("accountID")); // Set parent class id
                accounts.add(account);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all accounts: " + e.getMessage());
        }
        return accounts;
    }
    
    /**
     * Update an existing account
     * Explicit fields: name, balance, color (WHERE accountID = ?)
     */
    @Override
    public void update(Account account) {
        String sql = "UPDATE Account SET name = ?, balance = ?, color = ? WHERE accountID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, account.getName());
            pstmt.setDouble(2, account.getBalance());
            pstmt.setString(3, account.getColor());
            pstmt.setString(4, account.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating account: " + e.getMessage());
        }
    }
    
    /**
     * Delete an account by accountID
     */
    @Override
    public void delete(String accountID) {
        String sql = "DELETE FROM Account WHERE accountID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountID);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting account: " + e.getMessage());
        }
    }
    
    /**
     * Get total balance across all accounts
     */
    public double getTotalBalance() {
        String sql = "SELECT SUM(balance) as total FROM Account";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total balance: " + e.getMessage());
        }
        return 0.0;
    }
}
