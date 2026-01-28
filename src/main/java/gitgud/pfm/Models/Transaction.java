package gitgud.pfm.Models;

import java.time.LocalDateTime;

public class Transaction {
    private String ID;
    private String Categories;
    private double Amount;
    private String Name;
    private double Income;
    private String AccountID;
    private String Create_time;
    
    public Transaction(String ID, String Categories, double Amount, String Name, 
                      double Income, String AccountID, String Create_time) {
        this.ID = ID;
        this.Categories = Categories;
        this.Amount = Amount;
        this.Name = Name;
        this.Income = Income;
        this.AccountID = AccountID;
        this.Create_time = Create_time;
    }
    
    public String getID() { return ID; }
    public void setID(String ID) { this.ID = ID; }
    
    public String getCategories() { return Categories; }
    public void setCategories(String Categories) { this.Categories = Categories; }
    
    public double getAmount() { return Amount; }
    public void setAmount(double Amount) { this.Amount = Amount; }
    
    public String getName() { return Name; }
    public void setName(String Name) { this.Name = Name; }
    
    public double getIncome() { return Income; }
    public void setIncome(double Income) { this.Income = Income; }
    
    public String getAccountID() { return AccountID; }
    public void setAccountID(String AccountID) { this.AccountID = AccountID; }
    
    public String getCreate_time() { return Create_time; }
    public void setCreate_time(String Create_time) { this.Create_time = Create_time; }
}
