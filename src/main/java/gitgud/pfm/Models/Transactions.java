package gitgud.pfm.Models;

import java.time.LocalDateTime;

public class Transactions {
    private int id;
    private String type; // "expense", "income", "transfer"
    private String title;
    private String category;
    private double amount;
    private int accountId;
    private Integer toAccountId; // For transfers
    private LocalDateTime time;
    
    public Transactions(int id, String type, String title, String category, 
                      double amount, int accountId, Integer toAccountId, LocalDateTime time) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.accountId = accountId;
        this.toAccountId = toAccountId;
        this.time = time;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    
    public Integer getToAccountId() { return toAccountId; }
    public void setToAccountId(Integer toAccountId) { this.toAccountId = toAccountId; }
    
    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }
}
