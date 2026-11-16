package com.example.demo.event;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.event.dto.EventTemplateDTO;

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
}
