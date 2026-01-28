package gitgud.pfm.Models;

import gitgud.pfm.utils.IdGenerator;

public class Account extends FinancialEntity {
    private String accountID;
    private String color;

    // No-arg constructor for reflection mapping
    public Account() {
        super(null, null, 0.0);
    }
    
    public Account(String color, double balance, String name) {
        super(IdGenerator.generateAccountId(), name, balance);
        this.accountID = this.id; // Use the generated ID from parent
        this.color = color;
    }
    
    public String getAccountID() { return accountID; }
    public void setAccountID(String accountID) { this.accountID = accountID; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
