package com.app.thejavatest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.app.thejavatest.domain.Study;
import com.app.thejavatest.domain.StudyStatus;

/*
	https://junit.org/junit5/docs/current/user-guide/

	Method와 Class 레퍼런스를 사용해서 테스트 이름을 표기하는 방법 설정.
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
/*
	TestInstance
		- 테스트 클래스당 인스턴스를 하나만 만들어 사용한다.
		- 경우에 따라, 테스트 간에 공유하는 모든 상태를 @BeforeEach 또는 @AfterEach에서 초기화 할 필요가 있다.
		- @BeforeAll과 @AfterAll을 static 이 아닌 인스턴스 메소드 또는 인터페이스에 정의한 default 메소드로 정의할 수도 있다.
 */
// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
/*
	실행할 테스트 메소드 특정한 순서에 의해 실행되지만 어떻게 그 순서를 정하는지는 의도적으로 분명히 하지 않는다. (테스트 인스턴스를 테스트 마다 새로 만드는 것과 같은 이유)
	특정 순서대로 테스트를 실행하고 싶을 때도 있다.
		그 경우에는 테스트 메소드를 원하는 순서에 따라 실행하도록 @TestInstance(Lifecycle.PER_CLASS)와 함께 @TestMethodOrder를 사용할 수 있다.
		이 후 Order(N) 어노테이션을 사용하여 순서를 정할 수 있다.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
/*
	# 확장팩 등록 방법
		1. 선언적인 등록 @ExtendWith
		2. 프로그래밍 등록 @RegisterExtension
		3. 자동 등록 자바 ServiceLoader 이용
 */
// @ExtendWith(FindSlowTestExtension.class)
class StudyTest {

	@RegisterExtension
	static FindSlowTestExtension findSlowTestExtension = new FindSlowTestExtension();

	@Disabled
	@Test
	@DisplayName("제목_1 😸")
	void create_new_study_1() {
		Study study = new Study(StudyStatus.DRAFT, 15);
		/*
			message 를 작성하면 훨씬 파악하기 쉽다.

		    문자열 연산을 해야할 경우 람다를 사용하지 않으면 연산을 즉시 수행하여 그만큼 비용이 들지만,
			람다를 사용하면 바로 수행하는 것이 아닌 최대한 필요한 시점에 수행하기 때문에 비용적 측면을 고려하여 작성하자.
		 */

		assertAll(
			() -> assertNotNull(study, "스터디는 비어있으면 안됩니다."),
			() -> assertEquals(StudyStatus.DRAFT, study.getStatus(),
				() -> "스터디를 처음 만들면 " + StudyStatus.DRAFT + " 상태다."),
			() -> assertFalse(false),
			() -> assertTrue(study.getLimitCount() > 0, "스터디 최대 참석 가능 인원은 양수입니다.")
		);
		/*
			기본적으로 테스트는 위에서 부터 실행하며 위에가 실패하면 아래 테스트는 실행되지 않는다.
			-> assertAll 로 묶으면 한번에 실행시킬 수 있다.
		 */

		// assertTimeout : 특정 시간동안 테스트가 끝나지 않으면 테스트를 실패시키는 메소드
		assertTimeout(Duration.ofSeconds(10), () -> new Study(10)); // 여기서 block 이 걸린다.
		assertTimeoutPreemptively(Duration.ofSeconds(10),
			() -> new Study(10)); // 따로 스레드로 돌린다. (ThreadLocal 환경에서 주의해야한다. - ex. transaction)

		// ----assertj
		assertThat(study.getLimitCount()).isGreaterThan(10);
	}

	@Disabled
	@Test
	// 테스트를 실행하지 않음
	// @Disabled
	// 어떤 테스트인지 테스트 이름을 보다 쉽게 표현할 수 있는 방법을 제공하는 애노테이션. (@DisplayNameGeneration 보다 우선 순위가 높다)
	@DisplayName("제목_2 😸")
	void create_new_study_2() {
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class, () -> new Study(-10)
		);

		String message = exception.getMessage();
		assertEquals("limit은 0보다 커야한다.", message);
	}

	@Test
	@DisplayName("조건에 따라 테스트 실행하기_1")
	void test_1() {
		String test_env = System.getenv("TEST_ENV");
		assumeTrue("LOCAL".equalsIgnoreCase(test_env));

		assumingThat("LOCAL".equalsIgnoreCase(test_env), () -> {
			Study study = new Study(StudyStatus.DRAFT, 15);
			assertThat(study.getLimitCount()).isGreaterThan(10);
		});
	}

	@Disabled
	@Test
	@DisplayName("조건에 따라 테스트 실행하기_2")
	@EnabledOnOs({OS.MAC, OS.LINUX})
		// @EnabledIfEnvironmentVariable(named = "TEST_ENV", matches = "local")
		// @DisabledOnOs(OS.MAC)
		// @DisabledOnJre(JRE.JAVA_8)
	void test_2() {
		Study study = new Study(StudyStatus.DRAFT, 15);
		assertThat(study.getLimitCount()).isGreaterThan(10);
	}

	@Disabled
	@Test
	@DisplayName("태깅과 필터링 fast")
	// @Tag("fast")
	@FastTest
	void test_3() {
		/*
			https://binux.tistory.com/131 (gradle taging test 참고)
		 */
		Study study = new Study(StudyStatus.DRAFT, 15);
		assertThat(study.getLimitCount()).isGreaterThan(10);
	}

	@Disabled
	@Test
	@DisplayName("태깅과 필터링 slow")
	// @Tag("slow")
	@SlowTest
	void test_4() {
		Study study = new Study(StudyStatus.DRAFT, 15);
		assertThat(study.getLimitCount()).isGreaterThan(10);
	}

	@Disabled
	@DisplayName("테스트 반복하기_1")
	@RepeatedTest(value = 10, name = " {displayName}, {currentRepetition} / {totalRepetitions}")
	void test_5(RepetitionInfo repetitionInfo) {
		System.out.println(
			"test " + repetitionInfo.getCurrentRepetition() + "/" + repetitionInfo.getTotalRepetitions());
	}

	@Disabled
	@DisplayName("테스트 반복하기_2")
	@ParameterizedTest(name = "{index} {displayName} {0}")
	@ValueSource(strings = {"날씨가", "많이", "춥네요"})
	@EmptySource
		// 빈 문자열 인자
	void test_6(String message) {
		System.out.println("message = " + message);
	}

	@Disabled
	@DisplayName("테스트 반복하기_3_타입 변환")
	@ParameterizedTest(name = "{index} {displayName} {0}")
	@ValueSource(ints = {10, 20, 30})
	void test_7(@ConvertWith(StudyConverter.class) Study study) {
		System.out.println("study.getLimit() = " + study.getLimitCount());
	}

	/*
		static class : 단순히 외부 참조없이 내부 클래스를 접근하기 위한 용도이다.

		외부 참조의 2가지 단점
			1. 참조값을 담아야 하기 때문에, 인스턴스 생성시 시간적, 공간적으로 성능이 낮아진다.
			2. 외부 인스턴스에 대한 참조가 존재하기 때문에, 가비지 컬렉션이 인스턴스 수거를 하지 못하여 메모리 누수가 생길 수 있다.
	 */
	static class StudyConverter extends SimpleArgumentConverter {

		@Override
		protected Object convert(
			Object source,
			Class<?> targetType
		) throws ArgumentConversionException {
			assertEquals(Study.class, targetType);
			return new Study(Integer.parseInt(source.toString()));
		}
	}

	@Disabled
	@DisplayName("테스트 반복하기_4_암묵적 타입 변환")
	@ParameterizedTest(name = "{index} {displayName} {0}")
	@CsvSource({"10, '자바'", "20, '스프링'"})
	void test_8(ArgumentsAccessor argumentsAccessor) {
		Study study = new Study(argumentsAccessor.getInteger(0), argumentsAccessor.getString(1));
		System.out.println("study = " + study);
	}

	@Disabled
	@DisplayName("테스트 반복하기_5_암묵적 타입 변환")
	@ParameterizedTest(name = "{index} {displayName} {0}")
	@CsvSource({"10, '자바'", "20, '스프링'"})
	void test_9(@AggregateWith(StudyAggregator.class) Study study) {
		System.out.println("study = " + study);
	}

	// Aggregator 제약 : static or public class
	static class StudyAggregator implements ArgumentsAggregator {

		@Override
		public Object aggregateArguments(
			ArgumentsAccessor accessor,
			ParameterContext context
		) throws ArgumentsAggregationException {
			return new Study(accessor.getInteger(0), accessor.getString(1));
		}
	}

	/*
		Junit 기본 전략 : 테스트 메소드마다 새로운 인스턴스를 만든다!
			-> 테스트 간의 의존성을 없애기 위해서!
			- TestInstance 를 사용해 하나만 만들어 쓸 수 있다.
	 */
	int value = 1;

	@Order(2)
	@Test
	@DisplayName("테스트 인스턴스_1")
	void test_10() {
		System.out.println("this = " + this);
		Study study = new Study(StudyStatus.DRAFT, value++);
		System.out.println("study.getLimit() = " + study.getLimitCount());
	}

	@Order(1)
	@Test
	@DisplayName("테스트 인스턴스_2")
	void test_11() throws InterruptedException {
		Thread.sleep(1005L);
		System.out.println("this = " + this);
		Study study = new Study(StudyStatus.DRAFT, value++);
		System.out.println("study.getLimit() = " + study.getLimitCount());
	}

	// 모든 테스트를 실행하기 전 딱 한번 실행
	@BeforeAll
	static void beforeAll() { // static method 를 사용해야 한다.
		System.out.println("before all");
	}

	// 모든 테스트를 실행된 후 딱 한번 실행
	@AfterAll
	static void afterAll() { // static method 를 사용해야 한다.
		System.out.println("after all");
	}

	// 각각의 테스트를 실행하기 전 한번 실행
	@BeforeEach
	void beforeEach() {
		System.out.println("before each");
	}

	// 각각의 테스트를 실행된 후 한번 실행
	@AfterEach
	void afterEach() {
		System.out.println("after each");
	}
}