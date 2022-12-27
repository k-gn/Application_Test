package com.app.thejavatest.domain;

import lombok.Getter;

// getter or setter 둘중 하나라도 있어야 컨트롤러에서 매핑이된다. + 생성자도 필수
@Getter
// @NoArgsConstructor(force = true)
// @RequiredArgsConstructor
public class TestDto {

	private final String name;

	private final Integer age;

	// private 도 받아진다?!
	// Reflection은 접근 제어자와 상관 없이 클래스 객체를 동적으로 생성하는(런타임 시점) Java API이다.
	// Reflection은 무조건 기본 생성자가 필요하다. (java Reflection이 가져올 수 없는 정보 중 하나가 바로 생성자의 인자 정보들)
	private TestDto(
		String name,
		Integer age
	) {
		this.name = name;
		this.age = age;
	}
}
