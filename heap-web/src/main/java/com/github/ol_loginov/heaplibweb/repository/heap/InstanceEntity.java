package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.EntityInstance;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InstanceEntity implements EntityInstance {
	private int heapId;
	private long instanceId;
	private int instanceNumber;
	private long javaClassId;
	private boolean gcRoot;
	private long size;
	private long retainedSize;
	private long reachableSize;
}
