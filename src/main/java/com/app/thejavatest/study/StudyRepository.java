package com.app.thejavatest.study;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.thejavatest.domain.Study;

// jpa 가 알아서 구현체를 만들어줌
public interface StudyRepository extends JpaRepository<Study, Long> {
}
