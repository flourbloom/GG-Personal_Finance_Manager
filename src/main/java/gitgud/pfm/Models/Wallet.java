package gitgud.pfm.Models;

public class Wallet {
    private String AccountID;
    private String Color;
    private double Balance;
    private String Name;
    
    public Wallet(String AccountID, String Color, double Balance, String Name) {
        this.AccountID = AccountID;
        this.Color = Color;
        this.Balance = Balance;
        this.Name = Name;
    }
    
    public String getAccountID() { return AccountID; }
    public void setAccountID(String AccountID) { this.AccountID = AccountID; }
    
    public String getColor() { return Color; }
    public void setColor(String Color) { this.Color = Color; }
    
    public double getBalance() { return Balance; }
    public void setBalance(double Balance) { this.Balance = Balance; }
    
    public String getName() { return Name; }
    public void setName(String Name) { this.Name = Name; }
}