package gitgud.pfm.Models;

import java.util.HashMap;
import java.util.Map;

import gitgud.pfm.services.GenericSQLiteService;

public class Wallet {
    private String AccountID; //Primary Key
    private String Color;
    private double Balance;
    private String Name;
    
    public Wallet(String AccountID, String Color, double Balance, String Name) {
        this.AccountID = AccountID;
        this.Color = Color;
        this.Balance = Balance;
        this.Name = Name;

        Map<String, Object> config = new HashMap<>();
        config.put("class", Wallet.class);
        config.put("table", "Wallet");
        config.put("entity", this);
        GenericSQLiteService.create(config);
    }
    
    public String getColor() { return Color; }
    public void setColor(String Color) { this.Color = Color; }
    
    public double getBalance() { return Balance; }
    public void setBalance(double Balance) { this.Balance = Balance; }
    
    public String getName() { return Name; }
    public void setName(String Name) { this.Name = Name; }

    public String getAccountID() { return AccountID; }
    public void setAccountID(String AccountID) { this.AccountID = AccountID; }
}