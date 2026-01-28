package gitgud.pfm.CRUDs;

import gitgud.pfm.Models.Wallet;
import gitgud.pfm.services.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class WalletCRUD {
    
    public void saveWallet(Wallet wallet) {
        String insertSql = "INSERT INTO Wallets(AccountID, Color, Balance, Name) VALUES(?,?,?,?)";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, wallet.getAccountID());
            pstmt.setString(2, wallet.getColor());
            pstmt.setDouble(3, wallet.getBalance());
            pstmt.setString(4, wallet.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving wallet: " + e.getMessage());
        }
    }
    
    public List<Wallet> ShowAllWallets() {
        List<Wallet> wallets = new ArrayList<>();
        String sql = "SELECT * FROM Wallets";
        
        try (Connection conn = Database.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                wallets.add(mapResultSetToWallet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving wallets: " + e.getMessage());
        }
        return wallets;
    }
    
    public void deleteWallet(String accountID) {
        String sql = "DELETE FROM Wallets WHERE AccountID = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting wallet: " + e.getMessage());
        }
    }
    
    public void updateWallet(Wallet wallet) {
        String sql = "UPDATE Wallets SET Color = ?, Balance = ?, Name = ? WHERE AccountID = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, wallet.getColor());
            pstmt.setDouble(2, wallet.getBalance());
            pstmt.setString(3, wallet.getName());
            pstmt.setString(4, wallet.getAccountID());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating wallet: " + e.getMessage());
        }
    }
    
    private Wallet mapResultSetToWallet(ResultSet rs) throws SQLException {
        return new Wallet(
            rs.getString("AccountID"),
            rs.getString("Color"),
            rs.getDouble("Balance"),
            rs.getString("Name")
        );
    }
    
    public Wallet getWalletById(String accountID) {
        String sql = "SELECT * FROM Wallets WHERE AccountID = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToWallet(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving wallet: " + e.getMessage());
        }
        return null;
    }
    
    public boolean exists(String accountID) {
        String sql = "SELECT COUNT(*) FROM Wallets WHERE AccountID = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking wallet existence: " + e.getMessage());
        }
        return false;
    }
}
