package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.EntityInstance;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JavaClassEntity implements EntityInstance {
	private int heapId;
	private long javaClassId;

	private String name;
	private long allInstancesSize;
	private boolean array;
	private int instanceSize;
	private int instancesCount;
	private long retainedSizeByClass;
	private Long superClassId;
}
