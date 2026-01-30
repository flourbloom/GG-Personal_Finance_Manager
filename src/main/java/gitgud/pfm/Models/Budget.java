package gitgud.pfm.Models;

import gitgud.pfm.utils.IdGenerator;

public class Budget extends FinancialEntity {
    private double limitAmount;
    private String startDate;
    private String endDate;

    // No-arg constructor required for reflection-based mapping (do not auto-persist)
    public Budget() {
        super(null, null, 0.0);
    }
    
    public Budget(String name, double limitAmount, double balance, String startDate, String endDate) {
        super(IdGenerator.generateBudgetId(), name, balance);
        this.limitAmount = limitAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }
    
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
