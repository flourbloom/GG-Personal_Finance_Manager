package gitgud.pfm.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import gitgud.pfm.Models.Transaction;
import gitgud.pfm.interfaces.CRUDService;

public class TransactionService implements CRUDService<Transaction> {

    public TransactionService() {
    }

    @Override
    public void create(Transaction entity) {
        try (FileWriter fWriter = new FileWriter(new File("FauxDB.csv"), true)) {
                fWriter.write(entity.getID() + "," + entity.getAmount() + "," + entity.getName() + "\n");
        } catch (IOException e) {
            System.out.println("Error adding transaction: " + e.getMessage());
        }
    }

    @Override
    public Transaction read(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'read'");
    }

    @Override
    public void update(Transaction entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}