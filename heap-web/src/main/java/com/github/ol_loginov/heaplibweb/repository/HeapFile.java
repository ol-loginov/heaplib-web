package com.github.ol_loginov.heaplibweb.repository;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "HeapFile")
public class HeapFile extends SequenceIdentity {
	@Column(nullable = false)
	private String relativePath;
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private HeapFileStatus status;
	@Column(nullable = false)
	private Instant loadStart;
	@Column
	private Instant loadFinish;
	@Column
	private Float loadProgress;
	@Column(nullable = false)
	private String loadMessage = "";
	@Column
	private String loadError;
}
