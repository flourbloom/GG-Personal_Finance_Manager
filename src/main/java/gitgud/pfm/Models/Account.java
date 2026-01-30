package gitgud.pfm.Models;

import gitgud.pfm.utils.IdGenerator;

public class Account extends FinancialEntity {
    private String color;

    // No-arg constructor for reflection mapping
    public Account() {
        super(null, null, 0.0);
    }
    
    public Account(String color, double balance, String name) {
        super(IdGenerator.generateWalletId(), name, balance);
        this.color = color;
    }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
