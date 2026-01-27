package gitgud.pfm.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import gitgud.pfm.Interfaces.AddTransaction;

public class TransactionService implements AddTransaction {

    public TransactionService() {
    }

    @Override
    public void addTransaction(int i, double amount, String title) {        
        try (FileWriter fWriter = new FileWriter(new File("FuaxDB.csv"), true)) {
                fWriter.write(i + "," + amount + "," + title + "\n");
        } catch (IOException e) {
            System.out.println("Error adding transaction: " + e.getMessage());
        }
    }
}