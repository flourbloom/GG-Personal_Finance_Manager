package gitgud.pfm.FinanceAppcopy.model;

import java.time.LocalDateTime;

// Transaction wrapper class to adapt existing Transaction for FinanceApp compatibility
public class Transaction {
    private int id;
    private String type;
    private String description;
    private String category;
    private double amount;
    private int accountId;
    private String tags;
    private LocalDateTime timestamp;
    
    public Transaction(int id, String type, String description, String category, double amount, int accountId, String tags, LocalDateTime timestamp) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.accountId = accountId;
        this.tags = tags;
        this.timestamp = timestamp;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    // Alias methods for compatibility
    public String getTitle() { return description; }
    public LocalDateTime getTime() { return timestamp; }
}