package com.gopath.billing.gpis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, String> extends JpaRepository<T, String>, JpaSpecificationExecutor<T> {

    @Query(value = "from #{#entityName} as s where s.id = :id")
    public T getById(String id);
}
