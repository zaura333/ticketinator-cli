package com.ticketsystem.repository;

import com.ticketsystem.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Base repository that provides standard CRUD operations via Hibernate.
 * Each database operation opens its own session and transaction,
 * following the session-per-operation pattern suitable for a CLI app.
 *
 * @param <T>  entity type
 * @param <ID> primary key type
 */
public abstract class AbstractRepository<T, ID> implements GenericRepository<T, ID> {

    protected final SessionFactory sessionFactory;
    protected final Class<T> entityClass;

    protected AbstractRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    public T save(T entity) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            rollback(tx);
            throw new RuntimeException("Błąd zapisu encji: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(entityClass, id));
        }
    }

    @Override
    public List<T> findAll() {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM " + entityClass.getSimpleName();
            return session.createQuery(hql, entityClass).list();
        }
    }

    @Override
    public T update(T entity) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            T merged = session.merge(entity);
            tx.commit();
            return merged;
        } catch (Exception e) {
            rollback(tx);
            throw new RuntimeException("Błąd aktualizacji encji: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(T entity) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            T managed = session.merge(entity);
            session.remove(managed);
            tx.commit();
        } catch (Exception e) {
            rollback(tx);
            throw new RuntimeException("Błąd usunięcia encji: " + e.getMessage(), e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    protected void rollback(Transaction tx) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
            } catch (Exception ignored) {}
        }
    }
}
