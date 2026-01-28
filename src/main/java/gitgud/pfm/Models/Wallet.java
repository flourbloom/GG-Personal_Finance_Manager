package gitgud.pfm.Models;

import java.util.HashMap;
import java.util.Map;

import gitgud.pfm.services.GenericSQLiteService;

// TODO This is the renamed version of account
// So in the future commits, this should replace account related code

public class Wallet extends FinancialEntity {
    private String Color;

    public Wallet() {
        super(null, null, 0.0);
    }
    
    public Wallet(String walletId, String color, double balance, String name) {
        super(walletId, name, balance);
        this.Color = color;

        Map<String, Object> config = new HashMap<>();
        config.put("class", Wallet.class);
        config.put("table", "Wallet");
        config.put("entity", this);
        GenericSQLiteService.create(config);
    }
    
    public String getColor() { return Color; }
    public void setColor(String Color) { this.Color = Color; }
}