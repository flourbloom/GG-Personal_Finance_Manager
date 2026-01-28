package gitgud.pfm.FinanceAppcopy.model;

import java.time.LocalDate;

// Goal wrapper class to adapt existing Goal for FinanceApp compatibility
public class Goal {
    private int id;
    private String name;
    private double targetAmount;
    private double currentAmount;
    private LocalDate deadline;
    private String category;
    private boolean isPriority;
    private LocalDate createdDate;
    
    public Goal(int id, String name, double targetAmount, double currentAmount, LocalDate deadline, String category, boolean isPriority, LocalDate createdDate) {
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
        this.category = category;
        this.isPriority = isPriority;
        this.createdDate = createdDate;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }
    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isPriority() { return isPriority; }
    public void setPriority(boolean isPriority) { this.isPriority = isPriority; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    
    public double getProgress() {
        if (targetAmount <= 0) return 0;
        return (currentAmount / targetAmount) * 100;
    }
    
    // Alias methods for compatibility
    public String getTitle() { return name; }
    public double getCurrent() { return currentAmount; }
    public double getTarget() { return targetAmount; }
    public void setCurrent(double amount) { this.currentAmount = amount; }
}