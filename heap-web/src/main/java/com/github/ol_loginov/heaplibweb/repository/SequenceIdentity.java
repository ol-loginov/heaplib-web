package com.github.ol_loginov.heaplibweb.repository;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@MappedSuperclass
public abstract class SequenceIdentity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Override
	public int hashCode() {
		return getClass().getName().hashCode() * 31 + id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof SequenceIdentity sequenceIdentity && getClass().equals(obj.getClass())) {
			if (id > 0 || sequenceIdentity.id > 0) {
				return id == sequenceIdentity.id;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId();
	}
}
