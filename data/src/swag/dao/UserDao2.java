package swag.dao;

import javax.persistence.EntityManager;
import javax.persistence. EntityTransaction;

import swag.model.User;

public class UserDao2 implements IDao<User> {

	private EntityManager em;

	public UserDao2(EntityManager em) {
		this.em = em;
	}

	public User get(Long id) {
		return em.find(User.class, id);
	}

	public void create(User user) {

		EntityTransaction tx = em.getTransaction();
		tx.begin();

		em.persist(user);

		tx.commit();

	}

	public void update(User user) {

		EntityTransaction tx = em.getTransaction();
		tx.begin();

		em.merge(user);

		tx.commit();

	}

	public void delete(User user) {

		EntityTransaction tx = em.getTransaction();
		tx.begin();

		em.remove(user);

		tx.commit();

	}
	
	public boolean contains(User user)
	{
		return em.contains(user);
	}

}
