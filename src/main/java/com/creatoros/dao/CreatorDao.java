package com.creatoros.dao;

import java.util.Optional;

import com.creatoros.entity.Creator;

public interface CreatorDao {

    Creator save(Creator creator);

    Optional<Creator> findById(Long id);

    Optional<Creator> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByHandle(String handle);
}
