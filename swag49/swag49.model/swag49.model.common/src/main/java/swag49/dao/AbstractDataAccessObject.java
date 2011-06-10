package swag49.dao;

import org.slf4j.Logger;
import swag49.util.Log;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class AbstractDataAccessObject<T, PK> implements DataAccessObject<T, PK> {

    @Log
    private Logger log;

    private final Class<T> modelClass;

    public AbstractDataAccessObject(final Class<T> modelClass) {
        this.modelClass = modelClass;
    }

    protected abstract EntityManager getEntityManager();

    @Override
    public boolean contains(T message) {
        return getEntityManager().contains(message);
    }

    @Override
    public T create(T message) {
        return getEntityManager().merge(message);
    }

    @Override
    public void delete(T message) {
        message = getEntityManager().merge(message);
        getEntityManager().remove(message);
    }

    @Override
    public T get(PK id) {
        return getEntityManager().find(modelClass, id);
    }

    @Override
    public List<T> queryByExample(T model) {
        try {
            return findByExample(model, modelClass);
        } catch (Exception e) {
            log.warn("error finding by example", e);
            return Collections.emptyList();
        }
    }

    @Override
    public T update(T message) {
        return getEntityManager().merge(message);
    }

    private <T> List<T> findByExample(T example, Class<T> clazz)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException,
            NoSuchMethodException {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(clazz);
        Root<T> r = cq.from(clazz);
        Predicate p = cb.conjunction();
        Metamodel mm = em.getMetamodel();
        EntityType<T> et = mm.entity(clazz);
        Set<Attribute<? super T, ?>> attrs = et.getAttributes();
        for (Attribute<? super T, ?> a : attrs) {
            String name = a.getName();
            String javaName = a.getJavaMember().getName();
            String getter = "get" + javaName.substring(0, 1).toUpperCase() + javaName.substring(1);
            Method m = clazz.getMethod(getter, (Class<?>[]) null);

            if (m.invoke(example, (Object[]) null) != null) {
                boolean isCollection = Collection.class.isAssignableFrom(m.getReturnType());

                if (!isCollection)
                    p = cb.and(p, cb.equal(r.get(name), m.invoke(example, (Object[]) null)));
            }
        }
        cq.select(r).where(p);
        TypedQuery<T> query = em.createQuery(cq);
        return query.getResultList();
    }
}
