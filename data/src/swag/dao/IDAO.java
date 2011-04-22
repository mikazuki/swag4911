package swag.dao;

public interface IDAO<T> {

	public T get(Long id);
	public void create(T model);
	public void update(T model);
	public void delete(T model);

}