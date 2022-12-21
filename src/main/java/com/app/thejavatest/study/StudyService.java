package com.app.thejavatest.study;

import java.util.Optional;

import com.app.thejavatest.domain.Member;
import com.app.thejavatest.domain.Study;
import com.app.thejavatest.member.MemberService;

public class StudyService {

	// 서비스간의 의존관계도 있어서 인터페이스로 만드는게 좋아보인다.
	private final MemberService memberService;

	private final StudyRepository repository;

	public StudyService(MemberService memberService, StudyRepository repository) {
		assert memberService != null;
		assert repository != null;
		this.memberService = memberService;
		this.repository = repository;
	}

	public Study createNewStudy(Long memberId, Study study) {
		Optional<Member> member = memberService.findById(memberId);
		if (member.isPresent()) {
			study.setOwnerId(memberId);
		} else {
			throw new IllegalArgumentException("Member doesn't exist for id: '" + memberId + "'");
		}
		Study newstudy = repository.save(study);
		memberService.notify(newstudy);
		return newstudy;
	}

	public Study openStudy(Study study) {
		study.open();
		Study openedStudy = repository.save(study);
		memberService.notify(openedStudy);
		return openedStudy;
	}

	public void hi() {

	}
}
