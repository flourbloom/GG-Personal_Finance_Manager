package gitgud.pfm.CRUDs;

import gitgud.pfm.Models.Category;
import gitgud.pfm.interfaces.CRUDService;

public class CategoryCRUD implements CRUDService<Category> {

    @Override
    public void create(Category category) {
        // save to database or list
    }

    @Override
    public Category read(String id) {
        // find category by id
        return null;
    }

    @Override
    public void update(Category category) {

    }

    @Override
    public void delete(String id) {

    }
}


