package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.EntityIdentity;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class HeapEntity extends EntityIdentity {
	private int fileId;
	private Instant tm = Instant.now();
}
