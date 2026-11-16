package com.example.demo.event;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.event.dto.EventTemplateDTO;

@Repository
public class EventDAO {

	private final SqlSessionTemplate mybatis;
	public EventDAO(SqlSessionTemplate mybatis) {
		this.mybatis = mybatis;
	}
	
	public List<EventTemplateDTO> selectTemplates() {
		return mybatis.selectList("Event.selectTemplates");
	}
	
	
}
