package com.app.thejavatest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.app.thejavatest.domain.Member;
import com.app.thejavatest.domain.Study;
import com.app.thejavatest.member.MemberService;
import com.app.thejavatest.study.StudyRepository;
import com.app.thejavatest.study.StudyService;

/*
	단위 테스트의 단위의 기준은 사람마다 다르다.
	mocking 의 범위 기준도 사람마다 다르다.
	-> 프로젝트 시작 전 정하고 들어가면 된다.

	굳이 구현되어 있는 클래스를 목킹할 필요가 있을까?
		순수하게 해당 기능만 테스트 하고 싶을 때 적용할 수 있다. (다른 클래스와의 의존성으로 인해 내 기능을 확인하지 못하는 경우 목킹 사용)
		외부 클래스도 테스트 환경(서버)를 제공해주는 곳이 있고, 그렇지 않을 경우에 목킹을 적용하여 해결할 수 있다.

	Controller, Service, Repository 를 각각의 행위로 볼 수도 있고,
	Controller -> Service -> Repository 의 흐름을 하나의 행위로 볼 수도 있다.
 */
@ExtendWith(MockitoExtension.class)
public class MockTest {

	/*
		Mock 객체 만들기
			1. Mockito.mock() 메소드로 만드는 방법
			2. @Mock 애노테이션으로 만드는 방법
				1. JUnit 5 extension으로 MockitoExtension을 사용해야 한다.
				2. 필드
				3. 메소드 매개변수
	 */

	/*
		# Mock Stubbing

		- 모든 Mock 객체의 행동
			- Null 리턴
			- Optional 은 Optional.empty() 리턴
			- Primitive 타입은 기본 Primitive 값.
			- 콜렉션은 비어있는 콜렉션.
			- Void 메소드는 예외를 던지지 않고 아무런 일도 발생하지 않는다.

		Mock 객체를 조작해서
			- 특정한 매개변수를 받은 경우 특정한 값을 리턴하거나 예뢰를 던지도록 만들 수 있다.
				How about some stubbing?
				Argument matchers
			- Void 메소드 특정 매개변수를 받거나 호출된 경우 예외를 발생 시킬 수 있다.
				Subbing void methods with exceptions
			- 메소드가 동일한 매개변수로 여러번 호출될 때 각기 다르게 행동호도록 조작할 수도 있다.
				Stubbing consecutive calls
	 */

	@Mock
	MemberService memberService;

	@Mock
	StudyRepository studyRepository;

	@Test
	@DisplayName("mock test_1")
	void test_1() {
		MemberService memberService = mock(MemberService.class);
		StudyRepository studyRepository = mock(StudyRepository.class);
		StudyService studyService = new StudyService(memberService, studyRepository);
	}

	@Test
	@DisplayName("mock test_2")
	void test_2(
		@Mock MemberService memberService,
		@Mock StudyRepository studyRepository
	) {
		StudyService studyService = new StudyService(memberService, studyRepository);
		assertNotNull(studyService);

		Member member = new Member();
		member.setId(1L);
		member.setEmail("test@test.com");
		// stubbing
		// when(memberService.findById(1L)).thenReturn(Optional.of(member));

		// argument matcher
		when(memberService.findById(any())).thenReturn(Optional.of(member));
		Optional<Member> findById = memberService.findById(1L);
		assertEquals("test@test.com", findById.get().getEmail());

		Study study = new Study(10, "java");
		Study newStudy = studyService.createNewStudy(1L, study);

		// exception stubbing
		// when(memberService.findById(any())).thenThrow(new RuntimeException());

		// exception stubbing for void method
		doThrow(new IllegalArgumentException()).when(memberService).validate(any());
		assertThrows(IllegalArgumentException.class, () -> memberService.validate(1L));

		// 여러번 호출될 때 각기 다르게 행동
		// when(memberService.findById(any()))
		// 	.thenReturn(Optional.of(member))
		// 	.thenThrow(new IllegalArgumentException())
		// 	.thenReturn(Optional.empty());
	}

	@Test
	@DisplayName("mock test_3")
	void test_3() {
		StudyService studyService = new StudyService(memberService, studyRepository);
		assertNotNull(studyService);

		Member member = new Member();
		member.setId(1L);
		member.setEmail("test@test.com");

		Study study = new Study(10, "java");

		when(memberService.findById(1L)).thenReturn(Optional.of(member));
		when(studyRepository.save(study)).thenReturn(study);

		studyService.createNewStudy(1L, study);
		assertEquals(member.getId(), study.getOwnerId());

		/*
			# mock 객체 확인하기
			Mock 객체가 어떻게 사용이 됐는지 확인할 수 있다.
				특정 메소드가 특정 매개변수로 몇번 호출 되었는지, 최소 한번은 호출 됐는지, 전혀 호출되지 않았는지
					Verifying exact number of invocations
				어떤 순서대로 호출했는지
					Verification in order
				특정 시간 이내에 호출됐는지
					Verification with timeout
				특정 시점 이후에 아무 일도 벌어지지 않았는지
					Finding redundant invocations
		 */
		// 1번 호출
		verify(memberService, times(1)).notify(study);
		// 호출 X
		verify(memberService, never()).validate(any());
		// 순서
		InOrder inOrder = inOrder(memberService);
		inOrder.verify(memberService).notify(study);
		//inOrder.verify(memberService).notify(member);

		// 더이상 추가적인 액션이 없어야 할 경우
		verifyNoMoreInteractions(memberService);
	}

	/*
		# Mockito BDD 스타일 API
		BDD: 애플리케이션이 어떻게 “행동”해야 하는지에 대한 공통된 이해를 구성하는 방법으로, TDD에서 창안했다.

		행동에 대한 스팩
			Title
			Narrative
				As a  / I want / so that
			Acceptance criteria
				Given / When / Then

		Mockito는 BddMockito라는 클래스를 통해 BDD 스타일의 API를 제공한다.
	 */
	@Test
	@DisplayName("bdd test")
	void given_when_then() {
		// given
		StudyService studyService = new StudyService(memberService, studyRepository);
		assertNotNull(studyService);

		Member member = new Member();
		member.setId(1L);
		member.setEmail("test@test.com");

		Study study = new Study(10, "java");

		// when(memberService.findById(1L)).thenReturn(Optional.of(member));
		// when(studyRepository.save(study)).thenReturn(study);

		given(memberService.findById(1L)).willReturn(Optional.of(member));
		given(studyRepository.save(study)).willReturn(study);

		// when
		studyService.createNewStudy(1L, study);

		// then
		assertEquals(member.getId(), study.getOwnerId());
		// verify(memberService, times(1)).notify(study);
		// verifyNoMoreInteractions(memberService);

		then(memberService).should(times(1)).notify(study);
		then(memberService).shouldHaveNoMoreInteractions();
	}
}
