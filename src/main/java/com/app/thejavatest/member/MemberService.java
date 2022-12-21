package com.app.thejavatest.member;

import java.util.Optional;

import com.app.thejavatest.domain.Member;
import com.app.thejavatest.domain.Study;

public interface MemberService {

	Optional<Member> findById(Long memberId);

	void validate(Long memberId);

	void notify(Study newstudy);

	void notify(Member member);
}
