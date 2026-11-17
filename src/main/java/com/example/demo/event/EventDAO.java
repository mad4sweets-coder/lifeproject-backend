package com.example.demo.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.event.dto.EventChoiceDTO;
import com.example.demo.event.dto.EventTemplateDTO;
import com.example.demo.event.dto.GameEventDTO;

@Repository
public class EventDAO {

	private final SqlSessionTemplate mybatis;

	public EventDAO(SqlSessionTemplate mybatis) {
		this.mybatis = mybatis;
	}

	public List<EventTemplateDTO> selectTemplates() {
		return mybatis.selectList("Event.selectTemplates");
	}

	public Map<String, Object> selectCountryStat(String countryCode, int decade) {
		Map<String, Object> param = new HashMap<>();
		param.put("countryCode", countryCode);
		param.put("decade", decade);
		return mybatis.selectOne("Event.selectCountryStat", param);
	}

	public List<GameEventDTO> selectLifeCandidates(String templateId, int age, String gender, int wealth) {
		Map<String, Object> param = new HashMap<>();
		param.put("templateId", templateId);
		param.put("age", age);
		param.put("gender", gender);
		param.put("wealth", wealth);
		return mybatis.selectList("Event.selectLifeCandidates", param);
	}

	public List<String> selectExpIds(int lifeId) {
		return mybatis.selectList("Event.selectExpIds", lifeId);
	}

	public List<String> selectTexts(String templateId) {
		return mybatis.selectList("Event.selectTexts", templateId);
	}

	public String selectCountryName(String countryCode) {
		return mybatis.selectOne("Event.selectCountryName", countryCode);
	}

	// 선택 이벤트의 선택지 목록
	public List<EventChoiceDTO> selectChoices(String eventId) {
		return mybatis.selectList("Event.selectChoices", eventId);
	}

	// 선택지 하나
	public EventChoiceDTO selectChoice(int choiceId) {
		return mybatis.selectOne("Event.selectChoice", choiceId);
	}
}
