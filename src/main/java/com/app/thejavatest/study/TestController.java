package com.app.thejavatest.study;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.thejavatest.domain.TestDto;

@RestController
public class TestController {

	@PostMapping("/test")
	public ResponseEntity<?> testMapping(@RequestBody TestDto testDto) {
		return ResponseEntity.ok(testDto);
	}
}
