package com.github.ol_loginov.heaplibweb.repository;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeapFileRepository extends ListCrudRepository<HeapFile, Integer> {
	List<HeapFile> findAllByOrderByLoadStartDesc();

	List<HeapFile> findAllByStatusIn(List<HeapFileStatus> statuses);

	List<HeapFile> findAllByStatusNotIn(List<HeapFileStatus> statuses);
}
