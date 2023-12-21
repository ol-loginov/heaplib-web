package com.github.ol_loginov.heaplibweb.repository;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "HeapFileLoadEvent")
public class HeapFileLoadEvent extends SequenceIdentity {
	@Column(nullable = false)
	private Instant tm;
	@ManyToOne
	@JoinColumn(name = "heapFileId")
	private HeapFile heapFile;
	@Column
	private float progress = 0f;
	@Column(nullable = false)
	private String message = "";
}
