package com.example.demo.engine;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EngineController {

	private final EngineDao engineDao;
	private final EngineService engineService;

	// 생성자 주입: 스프링이 객체를 만들 때 EngineDao, EngineService를 넣어줌
	public EngineController(EngineDao engineDao, EngineService engineService) {
		this.engineDao = engineDao;
		this.engineService = engineService;
	}

	// DB 연결 테스트
	@GetMapping("/api/test/db")
	public String dbTest() {
		return "DB 연결 성공: " + engineDao.selectNow();
	}

	// C02 랜덤 출생 (브라우저 테스트용 GET → React 붙일 때 PostMapping으로 변경)
	@GetMapping("/api/engine/birth")
	public LifeDTO birth(@RequestParam int decade,
			@RequestParam(required = false) String guestToken) {
		return engineService.birth(decade, guestToken);
	}
}
