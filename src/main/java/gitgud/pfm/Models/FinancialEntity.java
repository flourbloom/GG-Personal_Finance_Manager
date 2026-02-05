package gitgud.pfm.Models;

/**
 * Abstract parent class for all financial entities (wallets, budgets, etc.)
 * Governs common features shared across different financial wallet types.
 */
public abstract class FinancialEntity {
    protected String id;
    protected String name;
    protected double balance;
    
    /**
     * Constructor for FinancialEntity
     * @param id Unique identifier for the financial entity
     * @param name Name of the financial entity
     * @param balance Current balance
     */
    public FinancialEntity(String id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public double getBalance() {
        return balance;
    }
    
    // Setters
    public void setId(String id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    /**
     * Adds amount to the balance
     * @param amount Amount to add
     */
    public void addToBalance(double amount) {
        this.balance += amount;
    }
    
    /**
     * Subtracts amount from the balance
     * @param amount Amount to subtract
     */
    public void subtractFromBalance(double amount) {
        this.balance -= amount;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                '}';
    }
}
