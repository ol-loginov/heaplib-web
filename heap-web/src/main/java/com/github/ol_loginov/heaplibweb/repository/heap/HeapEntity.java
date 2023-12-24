package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.EntityIdentity;
import com.github.ol_loginov.heaplibweb.repository.HeapFile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "Heap")
public class HeapEntity extends EntityIdentity {
	@ManyToOne(optional = false)
	@JoinColumn(name = "fileId", nullable = false, updatable = false)
	private HeapFile file;
	@Column(nullable = false)
	private Instant tm = Instant.now();
}
