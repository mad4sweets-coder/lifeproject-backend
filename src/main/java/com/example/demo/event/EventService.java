package com.example.demo.event;

import org.springframework.stereotype.Service;

@Service
public class EventService {

	private final EventDAO eDao;
	public EventService(EventDAO eDao) {
		this.eDao = eDao;
	}
}
