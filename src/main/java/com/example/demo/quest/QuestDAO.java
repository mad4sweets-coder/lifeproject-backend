package com.example.demo.quest;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

// C01 퀘스트 DB 접근 (SQL 은 resources/mappers/quest-mapper.xml, namespace "quest")
@Repository
public class QuestDAO {

	private final SqlSessionTemplate mybatis;

	public QuestDAO(SqlSessionTemplate mybatis) {
		this.mybatis = mybatis;
	}

	// 사용 중인 퀘스트 목록
	public List<QuestDTO> selectActiveQuests() {
		return mybatis.selectList("quest.selectActiveQuests");
	}

	// 이 인생이 내 것이고 진행 중이면 1
	public int countMyPlayingLife(int lifeId, int memberId) {
		return mybatis.selectOne("quest.countMyPlayingLife", Map.of("lifeId", lifeId, "memberId", memberId));
	}

	// 이 인생에 이미 정해진 퀘스트 개수
	public int countLifeQuests(int lifeId) {
		return mybatis.selectOne("quest.countLifeQuests", lifeId);
	}

	// 사용 중인 퀘스트면 1
	public int countActiveQuest(int questId) {
		return mybatis.selectOne("quest.countActiveQuest", questId);
	}

	// LIFE_QUEST 한 줄 저장
	public int insertLifeQuest(int lifeId, int questId) {
		return mybatis.insert("quest.insertLifeQuest", Map.of("lifeId", lifeId, "questId", questId));
	}
}
