package com.example.demo.event;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
