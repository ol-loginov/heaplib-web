package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.EntityInstance;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "Instance")
@IdClass(InstanceEntity.PK.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InstanceEntity implements EntityInstance {
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PK implements Serializable {
		private int heapId;
		private long instanceId;
	}

	@Id
	private int heapId;
	@Id
	private long instanceId;
	private int instanceNumber;
	private long javaClassId;
	private boolean gcRoot;
	private long size;
	private long retainedSize;
	private long reachableSize;
}
