package com.example.demo.event;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.engine.LifeDTO;
import com.example.demo.event.dto.EventResultDTO;
import com.example.demo.event.dto.EventTemplateDTO;
import com.example.demo.event.dto.GameEventDTO;

@RestController
@RequestMapping("/event")
public class EventController {

	private final EventService eServ;

	public EventController(EventService eServ) {
		this.eServ = eServ;
	}

	@GetMapping("/templates")
	public ResponseEntity<List<EventTemplateDTO>> getTemplates() {
		return ResponseEntity.ok(eServ.getTemplates());
	}

	// [테스트] 올해 일어난 이벤트 문장·효과
	// 예:
	// /event/test/roll?age=30&year=1960&country=IND&gender=F&wealth=3&name=프리야%20샤르마
	@GetMapping("/test/roll")
	public ResponseEntity<List<EventResultDTO>> testRoll(int age, int year, String country, String gender, int wealth,
			String name) {
		List<String> expIds = new ArrayList<>(); // 테스트: 아직 겪은 일 없음
		List<EventResultDTO> result = new ArrayList<>();

		for (EventTemplateDTO t : eServ.rollTemplates(age, year, country)) {
			GameEventDTO e = eServ.pickEvent(t, age, gender, wealth, expIds);
			if (e != null) {
				result.add(eServ.buildResult(t, e, name, age, year, country));
			}
		}
		return ResponseEntity.ok(result);
	}

	// 테스트: 엔진 연결용 rollYear 확인 (최종 전에 삭제)
	@GetMapping("/test/year")
	public List<EventResultDTO> testYear(@RequestParam int age, @RequestParam int birthYear,
			@RequestParam String country, @RequestParam String gender, @RequestParam int wealth,
			@RequestParam String name, @RequestParam(defaultValue = "0") int lifeId) {
		LifeDTO life = new LifeDTO();
		life.setLifeId(lifeId);
		life.setCurrentAge(age);
		life.setBirthYear(birthYear);
		life.setCurrentCountry(country);
		life.setGender(gender);
		life.setWealthClass(wealth);
		life.setName(name);
		return eServ.rollYear(life);
	}

	// 테스트: 선택지 고르기 (최종 전에 삭제)
	@GetMapping("/test/choice")
	public EventResultDTO testChoice(@RequestParam int choiceId, @RequestParam int age, @RequestParam int birthYear,
			@RequestParam String country, @RequestParam String name) {
		LifeDTO life = new LifeDTO();
		life.setCurrentAge(age);
		life.setBirthYear(birthYear);
		life.setCurrentCountry(country);
		life.setName(name);
		return eServ.applyChoice(life, choiceId, age);
	}
}
