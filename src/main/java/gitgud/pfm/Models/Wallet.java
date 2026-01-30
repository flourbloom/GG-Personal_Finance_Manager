package gitgud.pfm.Models;

import gitgud.pfm.utils.IdGenerator;

// TODO This is the renamed version of account
// So in the future commits, this should replace account related code

public class Wallet extends FinancialEntity {
    private String Color;

    public Wallet() {
        super(null, null, 0.0);
    }
    
    public Wallet(String color, double balance, String name) {
        super(IdGenerator.generateWalletId(), name, balance);
        this.Color = color;
    }
    
    public String getColor() { return Color; }
    public void setColor(String Color) { this.Color = Color; }
}