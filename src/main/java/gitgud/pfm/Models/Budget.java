package gitgud.pfm.Models;

import gitgud.pfm.utils.IdGenerator;

public class Budget extends FinancialEntity {
    public enum PeriodType {
        WEEKLY, MONTHLY, YEARLY, CUSTOM
    }
    
    private double limitAmount;
    private String startDate;
    private String endDate;
    private PeriodType periodType; // WEEKLY, MONTHLY, YEARLY, CUSTOM
    private String walletId; // Optional: link budget to specific wallet
    private String categoryId; // Optional: link budget to specific category

    // No-arg constructor required for reflection-based mapping (do not auto-persist)
    public Budget() {
        super(null, null, 0.0);
        this.periodType = PeriodType.MONTHLY; // Default to monthly
    }
    
    public Budget(String name, double limitAmount, double balance, String startDate, String endDate) {
        super(IdGenerator.generateBudgetId(), name, balance);
        this.limitAmount = limitAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.periodType = PeriodType.CUSTOM;
        this.walletId = null; // Wallet-wide by default
        this.categoryId = null;
    }
    
    public Budget(String name, double limitAmount, double balance, String startDate, 
                  String endDate, PeriodType periodType, String walletId) {
        super(IdGenerator.generateBudgetId(), name, balance);
        this.limitAmount = limitAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.periodType = periodType != null ? periodType : PeriodType.CUSTOM;
        this.walletId = walletId;
        this.categoryId = null;
    }
    
    public Budget(String name, double limitAmount, double balance, String startDate, 
                  String endDate, PeriodType periodType, String walletId, String categoryId) {
        super(IdGenerator.generateBudgetId(), name, balance);
        this.limitAmount = limitAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.periodType = periodType != null ? periodType : PeriodType.CUSTOM;
        this.walletId = walletId;
        this.categoryId = categoryId;
    }
    
    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }
    
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    
    public PeriodType getPeriodType() { return periodType; }
    public void setPeriodType(PeriodType periodType) { this.periodType = periodType; }
    
    public String getWalletId() { return walletId; }
    public void setWalletId(String walletId) { this.walletId = walletId; }
    
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    
    /**
     * Check if this budget is user account-wide (not linked to specific wallet)
     */
    public boolean isAccountWide() {
        return walletId == null || walletId.isEmpty();
    }
    
    /**
     * Check if this budget is for a specific category
     */
    public boolean isCategoryBudget() {
        return categoryId != null && !categoryId.isEmpty();
    }
}
