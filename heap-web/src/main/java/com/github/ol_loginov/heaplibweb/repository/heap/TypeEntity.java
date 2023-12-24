package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.EntityIdentity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "Type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
public class TypeEntity extends EntityIdentity {
	private int heapId;
	@Column(nullable = false)
	private String name;
}
