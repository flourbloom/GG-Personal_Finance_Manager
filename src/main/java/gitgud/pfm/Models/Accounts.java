package gitgud.pfm.Models;

abstract class Accounts {
    private int id;
    private String name;
    private String type; // bank,aba,cash.....
    private double balance;
    
    public Accounts(int id, String name, String type, double balance) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.balance = balance;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

}