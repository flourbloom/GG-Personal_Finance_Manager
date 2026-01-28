package gitgud.pfm.Models;

import gitgud.pfm.utils.IdGenerator;

public class Goal extends FinancialEntity {
    private double target;
    private String deadline;
    private double priority;
    private String createAt;
    
    // No-arg constructor required for reflection-based mapping (do not auto-persist)
    public Goal() {
        super(null, null, 0.0);
    }
    
    public Goal(String name, double target, double current, 
                String deadline, double priority, String createAt) {
        super(IdGenerator.generateGoalId(), name, current);
        this.target = target;
        this.deadline = deadline;
        this.priority = priority;
        this.createAt = createAt;
    }
    
    public double getTarget() { return target; }
    public void setTarget(double target) { this.target = target; }
    
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    
    public double getPriority() { return priority; }
    public void setPriority(double priority) { this.priority = priority; }
    
    public String getCreateAt() { return createAt; }
    public void setCreateAt(String createAt) { this.createAt = createAt; }
}
