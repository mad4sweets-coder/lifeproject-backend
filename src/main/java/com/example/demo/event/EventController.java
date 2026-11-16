package com.example.demo.event;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/event")
public class EventController {
	
	private final EventService eServ;
	public EventController(EventService eServ) {
		this.eServ = eServ;
	}

}
