package gitgud.pfm.services;

import gitgud.pfm.Models.Wallet;
import gitgud.pfm.interfaces.CRUDInterface;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * WalletService - Explicit CRUD operations for Wallet entity
 * All SQL queries explicitly show field mappings for clarity
 */
public class WalletService implements CRUDInterface<Wallet> {
    private final Connection connection;
    
    public WalletService() {
        this.connection = Database.getInstance().getConnection();
    }
    
    /**
     * Create a new wallet in the database
     * Explicit fields: walletId, name, balance, color
     */
    @Override
    public void create(Wallet wallet) {
        String sql = "INSERT INTO Wallet (id, name, balance, color) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, wallet.getId());
            pstmt.setString(2, wallet.getName());
            pstmt.setDouble(3, wallet.getBalance());
            pstmt.setString(4, wallet.getColor());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating wallet: " + e.getMessage());
        }
    }
    
    /**
     * Read a single wallet by walletId
     * Explicit fields: walletId, name, balance, color
     */
    @Override
    public Wallet read(String walletId) {
        String sql = "SELECT id, name, balance, color FROM Wallet WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, walletId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Wallet wallet = new Wallet();
                    wallet.setId(rs.getString("walletId"));
                    wallet.setName(rs.getString("name"));
                    wallet.setBalance(rs.getDouble("balance"));
                    wallet.setColor(rs.getString("color"));
                    wallet.setId(rs.getString("walletId")); // Set parent class id
                    return wallet;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading wallet: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Read all accounts from the database
     * Explicit fields: walletId, name, balance, color
     */
    public List<Wallet> readAll() {
        String sql = "SELECT id, name, balance, color FROM Wallet ORDER BY name";
        List<Wallet> wallets = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Wallet wallet = new Wallet();
                wallet.setId(rs.getString("id"));
                wallet.setName(rs.getString("name"));
                wallet.setBalance(rs.getDouble("balance"));
                wallet.setColor(rs.getString("color"));
                wallet.setId(rs.getString("id")); // Set parent class id
                wallets.add(wallet);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all wallets: " + e.getMessage());
        }
        return wallets;
    }
    
    /**
     * Update an existing wallet
     * Explicit fields: name, balance, color (WHERE id = ?)
     */
    @Override
    public void update(Wallet wallet) {
        String sql = "UPDATE Wallet SET name = ?, balance = ?, color = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, wallet.getName());
            pstmt.setDouble(2, wallet.getBalance());
            pstmt.setString(3, wallet.getColor());
            pstmt.setString(4, wallet.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating wallet: " + e.getMessage());
        }
    }
    
    /**
     * Delete a wallet by walletId
     */
    @Override
    public void delete(String walletId) {
        String sql = "DELETE FROM Wallet WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, walletId);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting wallet: " + e.getMessage());
        }
    }
    
    /**
     * Get total balance across all wallets
     */
    public double getTotalBalance() {
        String sql = "SELECT SUM(balance) as total FROM Wallet";
        
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
