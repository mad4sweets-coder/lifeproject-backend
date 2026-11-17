package com.example.demo.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

// A04 관리자 메인 집계 (쿼리는 resources/mappers/admin-mapper.xml)
@Mapper
public interface AdminDao {
	String selectRole(int memberId);					// 관리자인지 확인 (ADMIN / USER)
	int countTodayPlays();
	int countTotalPlays();
	List<CompletionDTO> selectCompletion();
	List<QuestRateDTO> selectQuestRates();
	List<TraitLevelDTO> selectTraitLevels();
	List<CountryLifeDTO> selectCountryLife();
}
