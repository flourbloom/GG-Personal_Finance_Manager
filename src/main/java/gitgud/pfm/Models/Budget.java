package gitgud.pfm.Models;

import gitgud.pfm.services.GenericSQLiteService;

import java.util.HashMap;
import java.util.Map;

public class Budget extends FinancialEntity {
    private double limits;
    private String start_date;
    private String end_date;
    private String trackedCategories;

    // No-arg constructor required for reflection-based mapping (do not auto-persist)
    public Budget() {
        super(null, null, 0.0);
    }
    
    public Budget(String id, String name, double limits, double balance, String start_date, String end_date,
            String trackedCategories) {
        super(id, name, balance);
        this.limits = limits;
        this.start_date = start_date;
        this.end_date = end_date;
        this.trackedCategories = trackedCategories;

        Map<String, Object> config = new HashMap<>();
        config.put("class", Budget.class);
        config.put("table", "Budget");
        config.put("entity", this);
        GenericSQLiteService.create(config);
    }
    
    public double getLimits() { return limits; }
    public void setLimits(double limits) { this.limits = limits; }
    
    public String getStart_date() { return start_date; }
    public void setStart_date(String start_date) { this.start_date = start_date; }
    
    public String getEnd_date() { return end_date; }
    public void setEnd_date(String end_date) { this.end_date = end_date; }
    
    public String getTrackedCategories() { return trackedCategories; }
    public void setTrackedCategories(String trackedCategories) { this.trackedCategories = trackedCategories; }
}
