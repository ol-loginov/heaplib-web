package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.SequenceIdentity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "JavaClass")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JavaClassEntity extends SequenceIdentity {
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "heapId", nullable = false, updatable = false)
	private HeapEntity heap;
	private String name;
	private long allInstancesSize;
	private boolean array;
	private int instanceSize;
	private int instancesCount;
	private long retainedSizeByClass;
	private long javaClassId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "superClassId", updatable = false)
	private JavaClassEntity superClass;
}
