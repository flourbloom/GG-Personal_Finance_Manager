package gitgud.pfm.Models;

import gitgud.pfm.utils.IdGenerator;
import java.util.HashMap;
import java.util.Map;

import gitgud.pfm.services.GenericSQLiteService;

public class Transaction{

    private String id; //Primary Key
    private String categoryId;
    private String name;
    private double income; // 1 for income, 0 for expense
    private double amount;
    private String accountId;
    private String createTime;

    // No-arg constructor required for reflection-based mapping
    public Transaction() {
        this(null, 0.0, null, 0.0, null, null);
    }
    
    public Transaction(String categoryId, double amount, String name, 
                      double income, String accountId, String createTime) {
        this.id = IdGenerator.generateTransactionId();
        this.amount = amount;
        this.name = name;
        this.categoryId = categoryId;
        this.income = income;
        this.accountId = accountId;
        this.createTime = createTime;

        // TODO this should either be deprecated or implemented properly
        // Map<String, Object> config = new HashMap<>();
        // config.put("class", Transaction.class);
        // config.put("table", "transaction_records");
        // config.put("entity", this);
        // GenericSQLiteService.create(config);
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
