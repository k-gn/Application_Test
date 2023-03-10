package com.app.thejavatest.study;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.thejavatest.domain.Study;

import lombok.RequiredArgsConstructor;

/*
	apache bench 로 간단한 테스트도 가능하다.
	99% 를 기준으로 본다.

	# JMeter

	https://jmeter.apache.org/
		- 성능 측정 및 부하 (load) 테스트 기능을 제공하는 오픈 소스 자바 애플리케이션.

	다양한 형태의 애플리케이션 테스트 지원
		웹 - HTTP, HTTPS
		SOAP / REST 웹 서비스
		FTP
		데이터베이스 (JDBC 사용)
		Mail (SMTP, POP3, IMAP)
		...

	CLI 지원
		CI 또는 CD 툴과 연동할 때 편리함.
		UI 사용하는 것보다 메모리 등 시스템 리소스를 적게 사용.

	주요 개념
		Thread Group: 한 쓰레드 당 유저 한명
		Sampler: 어떤 유저가 해야 하는 액션
		Listener: 응답을 받았을 할 일 (리포팅, 검증, 그래프 그리기 등)
		Configuration: Sampler 또는 Listener가 사용할 설정 값 (쿠키, JDBC 커넥션 등)
		Assertion: 응답이 성공적인지 확인하는 방법 (응답 코드, 본문 내용 등)

	대체제:
		Gatling
		nGrinder

	-----

	** 실제로 테스트할 서버는 jmeter 와 분리되어 있어야 한다!!

	첫 요청은 쓰레드 인스턴스를 만들기 위한 시간이 좀 걸린다.
	latency : 지연 시간

	Thread Group 만들기
		Number of Threads: 쓰레드 개수
		Ramp-up period: 쓰레드 개수를 만드는데 소요할 시간
		Loop Count: infinite 체크 하면 위에서 정한 쓰레드 개수로 계속 요청 보내기. 값을 입력하면 해당 쓰레드 개수 X 루프 개수 만큼 요청 보냄.

	Sampler 만들기
		여러 종류의 샘플러가 있지만 그 중에 우리가 사용할 샘플러는 HTTP Request 샘플러.
		HTTP Sampler
			요청을 보낼 호스트, 포트, URI, 요청 본문 등을 설정
		여러 샘플러를 순차적으로 등록하는 것도 가능하다.

	Listener 만들기
		View Results Tree
		View Resulrts in Table
		Summary Report
		Aggregate Report
		Response Time Graph
		Graph Results
		...

	Assertion 만들기
		응답 코드 확인
		응답 본문 확인

	CLI 사용하기
		jmeter -n -t 설정 파일 -l 리포트 파일
		-n : ui 비활성화
		-t : 테스트 설정파일 지정

	* BlazeMeter 같은 툴을 사용하여 동영상으로 액션을 녹화하여 jmeter 파일로 뽑을 수 있다.
	* cli 설정을 통해 jmeter 파일을 읽어 성능테스트를 자동화할 수도 있다!

 */
@RestController
@RequiredArgsConstructor
public class StudyController {

	final StudyRepository repository;

	@GetMapping("/study/{id}")
	public Study getStudy(@PathVariable Long id) {
		return repository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Study not found for '" + id + "'"));
	}

	@PostMapping("/study")
	public Study createsStudy(@RequestBody Study study) {
		return repository.save(study);
	}

}

/*
	# Chaos Monkey

	http://channy.creation.net/blog/1173
	https://codecentric.github.io/chaos-monkey-spring-boot/

	카오스 엔지니어링 툴
		프로덕션 환경, 특히 분산 시스템 환경에서 불확실성을 파악하고 해결 방안을 모색하는데 사용하는 툴

	운영 환경 불확실성의 예
		네트워크 지연
		서버 장애
		디스크 오작동
		메모리 누수
		...

	카오스 멍키 스프링 부트
		스프링 부트 애플리케이션에 카오스 멍키를 손쉽게 적용해 볼 수 있는 툴
		즉, 스프링 부트 애플리케이션을 망가트릴 수 있는 툴

	카오스 멍키 스프링 부트 주요 개념
		공격 대상 (Watcher)
			@RestController
			@Controller
			@Service
			@Repository
			@Component

		공격 유형 (Assaults)
			응답 지연 (Latency Assault)
			예외 발생 (Exception Assault)
			애플리케이션 종료 (AppKiller Assault)
			메모리 누수 (Memory Assault)


	# 응답 지연 이슈 재현 방법
	Repository Watcher 활성화
	chaos.monkey.watcher.repository=true

	카오스 멍키 활성화
	http post localhost:8080/actuator/chaosmonkey/enable

	카오스 멍키 활성화 확인
	http localhost:8080/actuator/chaosmonkey/status

	카오스 멍키 와처 확인
	http localhost:8080/actuator/chaosmonkey/watchers

	카오스 멍키 지연 공격 설정
	http POST localhost:8080/actuator/chaosmonkey/assaults level=3 latencyRangeStart=2000 latencyRangeEnd=5000 latencyActive=true
		level: 주기

	공격 확인
	http GET localhost:8080/actuator/chaosmonkey/assaults

	테스트
	JMeter 확인

	ex: B1 에 지연을 줄 경우 B2 에 A가 더 요청을 많이하는 지 테스트가 필요한 경우 (시스템의 견고함을 테스트를 통해 다질 수 있다.)
	A ->  B1, B2

	참고:
	https://codecentric.github.io/chaos-monkey-spring-boot/2.1.1/#_customize_watcher

	---
	에러 발생 재현 방법

	http POST localhost:8080/actuator/chaosmonkey/assaults level=3 latencyActive=false exceptionsActive=true exception.type=java.lang.RuntimeException

	https://codecentric.github.io/chaos-monkey-spring-boot/2.1.1/#_examples


 */