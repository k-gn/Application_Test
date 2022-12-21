package com.app.thejavatest.study;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.*;

import com.app.thejavatest.TheJavaTestApplication;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/*
	# 아키텍처 테스트

	# ArchUnit
	https://www.archunit.org/

	애플리케이션의 아키텍처를 테스트 할 수 있는 오픈 소스 라이브러리로, 패키지, 클래스, 레이어, 슬라이스 간의 의존성을 확인할 수 있는 기능을 제공한다.

	아키텍처 테스트 유즈 케이스
		A 라는 패키지가 B (또는 C, D) 패키지에서만 사용 되고 있는지 확인 가능.
		*Serivce라는 이름의 클래스들이 *Controller 또는 *Service라는 이름의 클래스에서만 참조하고 있는지 확인.
		*Service라는 이름의 클래스들이 ..service.. 라는 패키지에 들어있는지 확인.
		A라는 애노테이션을 선언한 메소드만 특정 패키지 또는 특정 애노테이션을 가진 클래스를 호출하고 있는지 확인.
		특정한 스타일의 아키텍처를 따르고 있는지 확인.

	주요 사용법
	1. 특정 패키지에 해당하는 클래스를 (바이트코드를 통해) 읽어들이고 (보통 루트 패키지)
	2. 확인할 규칙을 정의하고
	3. 읽어들인 클래스들이 그 규칙을 잘 따르는지 확인한다.

	JUnit 5 확장팩 제공
		@AnalyzeClasses: 클래스를 읽어들여서 확인할 패키지 설정
		@ArchTest: 확인할 규칙 정의

	* 순환 참조 없어야 한다.
		- 애플리케이션에서 순환참조는 가급적 줄이는게 좋다.
		- 내가 먼저 바뀌어야 하는지 상대가 먼저 바뀌어야 하는지에 대한 문제 발생
		- 일관된 흐름이 없어 파악 및 분석이 어렵다.
 */
@AnalyzeClasses(packagesOf = TheJavaTestApplication.class)
public class ArchTests {

	@ArchTest
	ArchRule controllerClassRule = classes().that().haveSimpleNameEndingWith("Controller")
		.should().accessClassesThat().haveSimpleNameEndingWith("Service")
		.orShould().accessClassesThat().haveSimpleNameEndingWith("Repository");

	// domain 은 study, member, domain 이 참조 가능
	@ArchTest
	ArchRule domainPackageRule = classes().that().resideInAPackage("..domain..") // 경로 상관없이 domain 이란 패키지에 속한 클래스들
		.should().onlyBeAccessed().byClassesThat()
		.resideInAnyPackage("..study..", "..member..", "..domain..");

	// domain 은 member 참조 불가능
	@ArchTest
	ArchRule memberPackageRule = noClasses().that().resideInAPackage("..domain..")
		.should().accessClassesThat().resideInAPackage("..member..");

	// study 는 study 만 참조 가능
	@ArchTest
	ArchRule studyPackageRule = noClasses().that().resideOutsideOfPackage("..study..")
		.should().accessClassesThat().resideInAnyPackage("..study..");

	// 순환참조는 없어야 한다
	@ArchTest
	ArchRule freeOfCycles = slices().matching("..thejavatest.(*)..")
		.should().beFreeOfCycles();

}