package gitgud.pfm.interfaces;

public interface CRUDInterface<T> {
    void create(T entity);
    T read(String id);
    void update(T entity);
    void delete(String id);
}
