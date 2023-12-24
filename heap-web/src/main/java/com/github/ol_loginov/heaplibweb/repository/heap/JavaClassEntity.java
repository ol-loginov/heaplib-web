package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.EntityInstance;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "JavaClass")
@IdClass(JavaClassEntity.PK.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JavaClassEntity implements EntityInstance {
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PK implements Serializable {
		private int heapId;
		private long javaClassId;
	}

	@Id
	private int heapId;
	@Id
	private long javaClassId;

	private String name;
	private long allInstancesSize;
	private boolean array;
	private int instanceSize;
	private int instancesCount;
	private long retainedSizeByClass;
	private Long superClassId;
}
