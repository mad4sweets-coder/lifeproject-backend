package com.example.demo.engine;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<?> birth(@RequestParam int decade,
			@RequestParam(required = false) String guestToken) {

		// 연대는 1940 ~ 현재 연대, 10년 단위만 허용
		int lastDecade = LocalDate.now().getYear() / 10 * 10;
		if (decade < 1940 || decade > lastDecade || decade % 10 != 0) {
			return ResponseEntity.badRequest()
					.body("연대는 1940 ~ " + lastDecade + " 사이 10년 단위로 보내주세요.");
		}

		LifeDTO life = engineService.birth(decade, guestToken);
		if (life == null) {
			return ResponseEntity.status(404)
					.body(decade + "년대 통계 데이터가 없어서 태어날 수 없어요.");
		}
		return ResponseEntity.ok(life);
	}

	// C03 1년 진행 (테스트용 GET → 나중에 PostMapping으로 변경)
	@GetMapping("/api/engine/next")
	public ResponseEntity<?> nextYear(@RequestParam int lifeId) {
		LifeDTO life = engineService.nextYear(lifeId);
		if (life == null) {
			return ResponseEntity.badRequest()
					.body("없는 인생이거나 이미 끝난 인생이에요.");
		}
		return ResponseEntity.ok(life);
	}

	// C04 집중 선택 (테스트용 GET → 나중에 PostMapping으로 변경)
	@GetMapping("/api/engine/focus")
	public ResponseEntity<?> chooseFocus(@RequestParam int lifeId, @RequestParam String focus) {
		LifeDTO life = engineService.chooseFocus(lifeId, focus);
		if (life == null) {
			return ResponseEntity.badRequest()
					.body("지금 나이대에서 고를 수 없는 집중이거나, 진행 중인 인생이 아니에요.");
		}
		return ResponseEntity.ok(life);
	}
}
