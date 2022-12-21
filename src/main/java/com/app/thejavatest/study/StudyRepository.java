package com.app.thejavatest.study;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.thejavatest.domain.Study;

public interface StudyRepository extends JpaRepository<Study, Long> {
}
