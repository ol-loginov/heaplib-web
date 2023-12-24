package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.EntityIdentity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "Field")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FieldEntity extends EntityIdentity {
	private int heapId;
	/**
	 * @see JavaClassEntity
	 */
	private long declaringClassId;
	@Column(nullable = false)
	private String name;
	private boolean staticFlag;
	/**
	 * Might be JavaClassEntity or internal primitive id
	 */
	private int typeId;

	@Override
	public String toString() {
		return idString() + " (class=" + declaringClassId + ",name=" + name + ")";
	}
}
