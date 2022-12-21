package com.app.thejavatest.study;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.File;
import java.util.Optional;

import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.app.thejavatest.domain.Member;
import com.app.thejavatest.domain.Study;
import com.app.thejavatest.domain.StudyStatus;
import com.app.thejavatest.member.MemberService;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
/*
	# Testcontainers (테스트에서 도커 컨테이너를 실행할 수 있는 라이브러리. https://www.testcontainers.org/)
		테스트 코드에서 컨테이너를 띄우고 내리는 작업을 알아서 해준다.
		테스트 실행시 DB를 설정하거나 별도의 프로그램 또는 스크립트를 실행할 필요 없다.
		보다 Production에 가까운 테스트를 만들 수 있다.
		테스트가 느려진다.

	@Testcontainers
		- JUnit 5 확장팩으로 테스트 클래스에 @Container를 사용한 필드를 찾아서 컨테이너 라이프사이클 관련 메소드를 실행해준다.

	@Container
		- 인스턴스 필드에 사용하면 모든 테스트 마다 컨테이너를 재시작 하고, 스태틱 필드에 사용하면 클래스 내부 모든 테스트에서 동일한 컨테이너를 재사용한다.

	---

	컨테이너 만들기
		New GenericContainer(String imageName)

	네트워크
		withExposedPorts(int...)
		getMappedPort(int)

	환경 변수 설정
		withEnv(key, value)

	명령어 실행
		withCommand(String cmd...)

	사용할 준비가 됐는지 확인하기
		waitingFor(Wait)
		Wait.forListeningPort()
		Wait.forHttp(String url) - application container 의 해당 url 로 요청 시 응답이 오는지?
		Wait.forLogMessage(String message) - 특정 로그메시지가 출력되는지?

	로그 살펴보기
		getLogs()
		followOutput()

	---

	# Testcontainers, 컨테이너 정보를 스프링 테스트에서 참조하기 (스프링이 알고 있게 설정)

	@ContextConfiguration
		스프링이 제공하는 애노테이션으로, 스프링 테스트 컨텍스트가 사용할 설정 파일 또는 컨텍스트를 커스터마이징할 수 있는 방법을 제공한다.

	ApplicationContextInitializer
		스프링 ApplicationContext를 프로그래밍으로 초기화 할 때 사용할 수 있는 콜백 인터페이스로, 특정 프로파일을 활성화 하거나, 프로퍼티 소스를 추가하는 등의 작업을 할 수 있다.

	TestPropertyValues
		테스트용 프로퍼티 소스를 정의할 때 사용한다.

	Environment
		스프링 핵심 API로, 프로퍼티와 프로파일을 담당한다.

	전체 흐름
		1. Testcontainer를 사용해서 컨테이너 생성
		2. ApplicationContextInitializer를 구현하여 생선된 컨테이너에서 정보를 축출하여 Environment에 넣어준다.
		3. @ContextConfiguration을 사용해서 ApplicationContextInitializer 구현체를 등록한다.
		4. 테스트 코드에서 Environment, @Value, @ConfigurationProperties 등 다양한 방법으로 해당 프로퍼티를 사용한다.

 */
@Testcontainers
@Slf4j
class StudyServiceTest {

	@Mock
	MemberService memberService;

	@Autowired
	StudyRepository studyRepository;

	// 테스트마다 만드는 건 불필요하니까 static 필드로 선언
	// @Container
	// private static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer()
	// 	.withDatabaseName("studytest");

	// 테스트 컨테이너가 지원하지 않는 컨테이너는 어떻게 만들까?
	@Container
	private static final GenericContainer postgreSQLContainer = new GenericContainer("postgres")
		.withExposedPorts(5432)
		.withEnv("POSTGRES_PASSWORD", "studytest")
		.withEnv("POSTGRES_DB", "studytest");

	// docker compose
	// @ClassRule // https://www.testcontainers.org/modules/docker_compose/
	// static DockerComposeContainer composeContainer =
	// 	new DockerComposeContainer(new File("src/test/resources/docker-compose.yml"))
	// 		.withLocalCompose(true)
	// 		.withExposedService("study-db", 5432);

	@BeforeEach
	void beforeEach() {
		studyRepository.deleteAll();
	}

	@BeforeAll
	static void beforeAll() {
		// log streaming
		Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(log);
		postgreSQLContainer.followOutput(logConsumer);
	}

	@Test
	void createNewStudy() {
		System.out.println("========");
		System.out.println("postgreSQLContainer = " + postgreSQLContainer.getMappedPort(5432));
		// System.out.println("composeContainer = " + composeContainer.getServicePort("study-db", 5432));

		// Given
		StudyService studyService = new StudyService(memberService, studyRepository);
		assertNotNull(studyService);

		Member member = new Member();
		member.setId(1L);
		member.setEmail("keesun@email.com");

		Study study = new Study(10, "테스트");

		given(memberService.findById(1L)).willReturn(Optional.of(member));

		// When
		studyService.createNewStudy(1L, study);

		// Then
		assertEquals(1L, study.getOwnerId());
		then(memberService).should(times(1)).notify(study);
		then(memberService).shouldHaveNoMoreInteractions();
	}

	@DisplayName("다른 사용자가 볼 수 있도록 스터디를 공개한다.")
	@Test
	void openStudy() {
		// Given
		StudyService studyService = new StudyService(memberService, studyRepository);
		Study study = new Study(10, "더 자바, 테스트");
		assertNull(study.getOpenedDateTime());

		// When
		studyService.openStudy(study);

		// Then
		assertEquals(StudyStatus.OPENED, study.getStatus());
		assertNotNull(study.getOpenedDateTime());
		then(memberService).should().notify(study);
	}

}