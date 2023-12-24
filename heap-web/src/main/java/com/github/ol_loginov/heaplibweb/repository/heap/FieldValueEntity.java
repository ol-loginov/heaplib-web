package com.github.ol_loginov.heaplibweb.repository.heap;

import com.github.ol_loginov.heaplibweb.repository.EntityInstance;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "FieldValue")
@IdClass(FieldValueEntity.PK.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FieldValueEntity implements EntityInstance {
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PK implements Serializable {
		private long javaClassId;
		private long definingInstanceId;
		private int fieldId;
	}

	@Id
	private long javaClassId;
	@Id
	private long definingInstanceId;
	@Id
	private int fieldId;
	private boolean staticFlag;
	private String value;
	private Long valueInstanceId;
}
