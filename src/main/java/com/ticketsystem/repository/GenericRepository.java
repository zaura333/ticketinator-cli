package com.ticketsystem.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD contract for all repositories.
 *
 * @param <T>  entity type
 * @param <ID> primary key type
 */
public interface GenericRepository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    T update(T entity);

    void delete(T entity);
}
