package com.github.ol_loginov.heaplibweb.repository.heap;


import com.github.ol_loginov.heaplibweb.repository.HeapFile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HeapRepository extends CrudRepository<HeapEntity, Integer> {
	Optional<HeapEntity> findOneByFile(HeapFile heapFile);
}
