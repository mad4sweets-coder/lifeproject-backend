package com.example.demo.result;

import org.springframework.stereotype.Service;

@Service
public class ResultService {

	private final ResultDao resultDao;

	// 생성자 주입: 스프링이 객체를 만들 때 ResultDao를 넣어줌
	public ResultService(ResultDao resultDao) {
		this.resultDao = resultDao;
	}

	// ================= C18 인생 결과 =================
	// 끝난 인생 1개의 결과를 한 번에 모음 (요약 + 그래프 + 인생 기록 + 퀘스트)
	// 인생이 없으면 null
	public ResultDTO getResult(int lifeId) {
		ResultDTO result = resultDao.selectResult(lifeId);
		if (result == null) {
			return null;
		}
		result.setYears(resultDao.selectYearLogs(lifeId));		// 능력치 그래프 (LIFE_YEAR_LOG)
		result.setEvents(resultDao.selectEventLogs(lifeId));	// 인생 기록 전체 (LIFE_EVENT_LOG)
		result.setQuests(resultDao.selectQuests(lifeId));		// 퀘스트 달성 여부 (LIFE_QUEST, 비회원은 빈 목록)
		result.setPointEarned(resultDao.selectEarnedPoint(lifeId));	// 받은 포인트 (POINT_HISTORY)
		return result;
	}
}
