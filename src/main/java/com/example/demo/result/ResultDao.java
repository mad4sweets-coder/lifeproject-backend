package com.example.demo.result;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

// C18 인생 결과 조회 (쿼리는 resources/mappers/result-mapper.xml)
@Mapper
public interface ResultDao {
	ResultDTO selectResult(int lifeId);
	List<YearLogDTO> selectYearLogs(int lifeId);
	List<EventLogDTO> selectEventLogs(int lifeId);
	List<QuestResultDTO> selectQuests(int lifeId);
	int selectEarnedPoint(int lifeId);
}
