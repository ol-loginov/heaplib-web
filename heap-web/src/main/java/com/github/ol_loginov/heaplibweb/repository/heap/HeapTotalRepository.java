package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.SequenceIdentity;
import org.springframework.stereotype.Repository;

@Repository
public interface HeapTotalRepository {
	void persist(SequenceIdentity entity);
}
