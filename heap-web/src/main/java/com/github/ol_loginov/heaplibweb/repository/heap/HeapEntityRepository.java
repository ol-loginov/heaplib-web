package com.github.ol_loginov.heaplibweb.repository.heap;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeapEntityRepository extends CrudRepository<HeapEntity, Integer> {
}
