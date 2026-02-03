package gitgud.pfm.Models;

import gitgud.pfm.utils.IdGenerator;

public class Goal extends FinancialEntity {
    private double target;
    private double priority;
    private String createTime;
    private String deadline;
    
    // No-arg constructor required for reflection-based mapping (do not auto-persist)
    public Goal() {
        super(null, null, 0.0);
    }
    
    public Goal(String name, double target, double current, 
                String deadline, double priority, String createTime) {
        super(IdGenerator.generateGoalId(), name, current);
        this.target = target;
        this.deadline = deadline;
        this.priority = priority;
        this.createTime = createTime;
    }

    /**
     * New constructor used by CLI: do not require current balance (computed from transactions)
     */
    public Goal(String name, double target, String deadline, double priority, String createTime) {
        super(IdGenerator.generateGoalId(), name, 0.0);
        this.target = target;
        this.deadline = deadline;
        this.priority = priority;
        this.createTime = createTime;
    }
    
    public double getTarget() { return target; }
    public void setTarget(double target) { this.target = target; }
    
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    
    public double getPriority() { return priority; }
    public void setPriority(double priority) { this.priority = priority; }
    
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
