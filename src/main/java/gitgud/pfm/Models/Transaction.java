package gitgud.pfm.Models;

import gitgud.pfm.utils.IdGenerator;
import java.util.HashMap;
import java.util.Map;

import gitgud.pfm.services.GenericSQLiteService;

public class Transaction extends FinancialEntity{
    // No-arg constructor required for reflection-based mapping
    public Transaction() {
        super(null, null, 0.0);
    }
    private String id; // Primary Key
    private String categoryId;
    private double amount;
    private String name;
    private double income;
    private String accountId;
    private String createTime;
    
    public Transaction(String categoryId, double amount, String name, 
                      double income, String accountId, String createTime) {
        super(IdGenerator.generateTransactionId(),
              name,
              amount);
        this.categoryId = categoryId;
        this.income = income;
        this.accountId = accountId;
        this.createTime = createTime;

        Map<String, Object> config = new HashMap<>();
        config.put("class", Transaction.class);
        config.put("table", "transaction_records");
        config.put("entity", this);
        GenericSQLiteService.create(config);
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