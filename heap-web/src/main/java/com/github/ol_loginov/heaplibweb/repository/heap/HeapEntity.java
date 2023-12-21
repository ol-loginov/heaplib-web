package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.HeapFile;
import com.github.ol_loginov.heaplibweb.repository.SequenceIdentity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
@Entity
@Table(name = "Heap")
public class HeapEntity extends SequenceIdentity {
	@ManyToOne(optional = false)
	@JoinColumn(name = "fileId", nullable = false, updatable = false)
	private HeapFile file;
	@Column(nullable = false)
	private Instant tm = Instant.now();
}
