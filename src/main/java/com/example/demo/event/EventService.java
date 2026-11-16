package com.example.demo.event;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.event.dto.EventTemplateDTO;

@Service
public class EventService {

	private final EventDAO eDOA;
	public EventService(EventDAO eDOA) {
		this.eDOA = eDOA;
	}
	
	private List<EventTemplateDTO> templateCache;
	
	public List<EventTemplateDTO> getTemplates() {
		if (templateCache == null) {
			templateCache = eDOA.selectTemplates();
		}
		return templateCache;
	}
	
	public void clearTemplateCache() {
		templateCache = null;
	}
}
