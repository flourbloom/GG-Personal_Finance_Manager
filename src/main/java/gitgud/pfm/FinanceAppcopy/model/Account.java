package gitgud.pfm.FinanceAppcopy.model;

// Account wrapper class to adapt Wallet for FinanceApp compatibility
public class Account {
    private int id;
    private String name;
    private String type;
    private double balance;
    private String color;
    
    public Account(int id, String name, String type, double balance, String color) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.balance = balance;
        this.color = color;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}