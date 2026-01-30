package gitgud.pfm.interfaces;

// TODO this is going to be used by the services introduced in the future

public interface CRUDInterface<T> {
    void create(T entity);
    T read(String id);
    void update(T entity);
    void delete(String id);
}
