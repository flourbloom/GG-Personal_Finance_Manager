package gitgud.pfm.Models;

public class Account extends FinancialEntity {
    private String accountID;
    private String color;

    // No-arg constructor for reflection mapping
    public Account() {
        super(null, null, 0.0);
    }
    
    public Account(String accountID, String color, double balance, String name) {
        super(accountID, name, balance);
        this.accountID = accountID;
        this.color = color;
    }
    
    public String getAccountID() { return accountID; }
    public void setAccountID(String accountID) { this.accountID = accountID; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
