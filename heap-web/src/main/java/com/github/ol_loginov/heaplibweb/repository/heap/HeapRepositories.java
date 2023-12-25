package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.EntityInstance;
import org.springframework.jdbc.core.JdbcTemplate;

public interface HeapRepositories {
	int NUMBER_NOT_READY = -1;

	void persist(EntityInstance entity);

	void flush();

	JdbcTemplate getJdbc();

	JavaClassRepository getJavaClasses();

	FieldRepository getFields();

	FieldValueRepository getFieldValues();

	TypeRepository getTypes();

	InstanceRepository getInstances();

	static int shouldBeReady(int entityAttribute) {
		if (entityAttribute == NUMBER_NOT_READY) {
			throw new UnsupportedOperationException("number not ready");
		}
		return entityAttribute;
	}

	static long shouldBeReady(long entityAttribute) {
		if (entityAttribute == NUMBER_NOT_READY) {
			throw new UnsupportedOperationException("number not ready");
		}
		return entityAttribute;
	}
}
