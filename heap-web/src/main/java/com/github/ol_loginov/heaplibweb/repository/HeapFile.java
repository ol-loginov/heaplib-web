package com.github.ol_loginov.heaplibweb.repository;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "HeapFile")
public class HeapFile extends SequenceIdentity {
	@Column(nullable = false)
	private String relativePath;
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private HeapFileStatus status = HeapFileStatus.PENDING;
	@Column(nullable = false)
	private Instant loadStart = Instant.now();
	@Column
	private Instant loadFinish;
	@Column
	private Float loadProgress;
	@Column(nullable = false)
	private String loadMessage = "";
	@Column
	private String loadError;
}
