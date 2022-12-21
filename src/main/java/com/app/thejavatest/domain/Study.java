package com.app.thejavatest.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Study {

	@Id
	@GeneratedValue
	private Long id;

	private StudyStatus status = StudyStatus.DRAFT;

	private int limitCount;

	private String name;

	private LocalDateTime openedDateTime;

	private Long ownerId;

	public Study(
		int limitCount,
		String name
	) {
		this.limitCount = limitCount;
		this.name = name;
	}

	public Study(int limitCount) {
		if (limitCount < 0) {
			throw new IllegalArgumentException("limit은 0보다 커야한다.");
		}
		this.limitCount = limitCount;
	}

	public Study(
		StudyStatus status,
		int limitCount
	) {
		this(limitCount);
		this.status = status;
	}

	public void open() {
		this.openedDateTime = LocalDateTime.now();
		this.status = StudyStatus.OPENED;
	}
}
