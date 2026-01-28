package gitgud.pfm.CRUDs;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.services.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;
import gitgud.pfm.Models.Wallet;


public class TransactionCRUD {
    
    public void saveTransaction(Transaction transaction) {
      String insertSql = "INSERT INTO transaction_records(ID, Categories, Amount, Name, Income, AccountID, Create_time) VALUES(?,?,?,?,?,?,?)";
      try (Connection conn = Database.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(insertSql))
            {
                pstmt.setString(1, transaction.getID());
                pstmt.setString(2, transaction.getCategories());
                pstmt.setDouble(3, transaction.getAmount());
                pstmt.setString(4, transaction.getName());
                pstmt.setDouble(5, transaction.getIncome());
                pstmt.setString(6, transaction.getAccountID());
                pstmt.setString(7, transaction.getCreate_time());
                pstmt.executeUpdate();
    } catch (SQLException e) {
        System.out.println("Error saving transaction: " + e.getMessage());
    }
    }
    public List<Transaction> ShowAllTransaction() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transaction_records ORDER BY Timestamp DESC";
        
        try (Connection conn = Database.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving transactions: " + e.getMessage());
        }
        return transactions;
    }
    public void deleteTransaction(String transactionId) {
        String sql = "DELETE FROM transaction_records WHERE ID = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transactionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting transaction: " + e.getMessage());
        }
    }
    public void updateTransaction(Transaction transaction) {
        String sql = "UPDATE transaction_records SET Categories = ?, Amount = ?, Name = ?, Income = ?, AccountID = ?, Create_time = ? WHERE ID = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getCategories());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getName());
            pstmt.setDouble(4, transaction.getIncome());
            pstmt.setString(5, transaction.getAccountID());
            pstmt.setString(6, transaction.getCreate_time());
            pstmt.setString(7, transaction.getID());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating transaction: " + e.getMessage());
        }
    }
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getString("ID"),
            rs.getString("Categories"),
            rs.getDouble("Amount"),
            rs.getString("Name"),
            rs.getDouble("Income"),
            rs.getString("AccountID"),
            rs.getString("Create_time")
        );
    }
    public List<Transaction> GetTransactionByWallet(Wallet wallet){
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transaction_records WHERE AccountID = ? ORDER BY Create_time DESC";
        try (Connection conn = Database.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, wallet.getAccountID());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
            transactions.add(mapResultSetToTransaction(rs));
            }
            return transactions;
    }
    catch (SQLException e) {
        System.out.println("Error retrieving transactions for wallet: " + e.getMessage());
    }
    return null;
    }
    public boolean exists(String transactionId) {
        String sql = "SELECT COUNT(*) FROM transaction_records WHERE ID = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transactionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking transaction existence: " + e.getMessage());
        }
        return false;
    }

}