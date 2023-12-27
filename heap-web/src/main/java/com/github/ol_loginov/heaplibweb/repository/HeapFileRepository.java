package com.github.ol_loginov.heaplibweb.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeapFileRepository {
	List<HeapFile> findAllByOrderByLoadStartDesc();

	List<HeapFile> findAllByStatusIn(List<HeapFileStatus> statuses);

	List<HeapFile> findAllByStatusNotIn(List<HeapFileStatus> statuses);

	Optional<HeapFile> findFirstByRelativePathOrderByIdDesc(String relativePath);
}
