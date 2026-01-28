package gitgud.pfm.Models;

public class Goal {
    private String id;
    private String name;
    private double target;
    private double current;
    private String deadline;
    private double priority;
    private String createAt;
    
    public Goal(String id, String name, double target, double current, 
                String deadline, double priority, String createAt) {
        this.id = id;
        this.name = name;
        this.target = target;
        this.current = current;
        this.deadline = deadline;
        this.priority = priority;
        this.createAt = createAt;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getTarget() { return target; }
    public void setTarget(double target) { this.target = target; }
    
    public double getCurrent() { return current; }
    public void setCurrent(double current) { this.current = current; }
    
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    
    public double getPriority() { return priority; }
    public void setPriority(double priority) { this.priority = priority; }
    
    public String getCreateAt() { return createAt; }
    public void setCreateAt(String createAt) { this.createAt = createAt; }
}
