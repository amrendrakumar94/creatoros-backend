package com.creatoros.dao;

import com.creatoros.entity.Creator;

import java.util.Optional;

public interface CreatorDao {

    /** Persists a new creator or merges a detached one. Always use the returned instance. */
    Creator save(Creator creator);

    Optional<Creator> findById(Long id);

    Optional<Creator> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByHandle(String handle);
}
