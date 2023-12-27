package com.github.ol_loginov.heaplibweb.repository;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class HeapFile extends EntityIdentity {
	private String relativePath;
	private HeapFileStatus status = HeapFileStatus.PENDING;
	private Instant loadStart = Instant.now();
	private Instant loadFinish;
	private Float loadProgress;
	private String loadMessage = "";
	private String loadError;
}
