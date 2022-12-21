package com.app.thejavatest.study;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import javax.persistence.Entity;

import com.app.thejavatest.TheJavaTestApplication;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packagesOf = TheJavaTestApplication.class)
public class ArchClassTests {

	@ArchTest
	ArchRule controllerClassRule = classes().that().haveSimpleNameEndingWith("Controller")
		.should().accessClassesThat().haveSimpleNameEndingWith("Service")
		.orShould().accessClassesThat().haveSimpleNameEndingWith("Repository");

	@ArchTest
	ArchRule repositoryClassRule = noClasses().that().haveSimpleNameEndingWith("Repository")
		.should().accessClassesThat().haveSimpleNameEndingWith("Service");

	@ArchTest
	ArchRule studyClassesRule = classes().that().haveSimpleNameStartingWith("Study")
		.and().areNotEnums()
		.and().areNotAnnotatedWith(Entity.class)
		.should().resideInAnyPackage("..study..");
}



/*

	이밖에도.. 다양한 애플리케이션 테스트 방법들이 존재한다.

	Selenium WebDriver
		웹 브라우저 기반 자동화된 테스트 작성에 사용할 수 있는 툴

	DBUnit
		데이터베이스에 데이터를 CVS, Excel 등으로 넣어주는 툴

	REST Assured
		REST API 테스트 라이브러리

	Cucumber
		BDD를 지원하는 테스트 라이브러리.

 */