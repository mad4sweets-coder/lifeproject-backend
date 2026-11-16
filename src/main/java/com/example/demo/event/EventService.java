package com.example.demo.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.event.dto.EventResultDTO;
import com.example.demo.event.dto.EventTemplateDTO;
import com.example.demo.event.dto.GameEventDTO;

@Service
public class EventService {

	private final EventDAO eDAO;

	public EventService(EventDAO eDAO) {
		this.eDAO = eDAO;
	}

	// ===== 1단계: 템플릿 목록 (한 번만 DB 조회) =====

	private List<EventTemplateDTO> templateCache;

	public List<EventTemplateDTO> getTemplates() {
		if (templateCache == null) {
			templateCache = eDAO.selectTemplates();
		}
		return templateCache;
	}

	public void clearTemplateCache() {
		templateCache = null;
	}

	// ===== 2단계: 템플릿별 확률 계산 + 굴리기 =====

	// 올해 걸린 템플릿 목록
	public List<EventTemplateDTO> rollTemplates(int age, int year, String countryCode) {
		Map<String, Object> stat = eDAO.selectCountryStat(countryCode, year / 10 * 10);
		List<EventTemplateDTO> hits = new ArrayList<>();

		for (EventTemplateDTO t : getTemplates()) {
			if (age < t.getMinAge() || age > t.getMaxAge())
				continue; // 나이 조건

			double prob = calcProbability(t, age, stat);
			if (Math.random() < prob) {
				hits.add(t);
			}
		}
		return hits;
	}

	// 템플릿 하나의 올해 발생 확률
	public double calcProbability(EventTemplateDTO t, int age, Map<String, Object> stat) {
		double base = t.getBaseProb() == null ? 0 : t.getBaseProb();

		switch (t.getProbMethod()) {
		case "TFR": // 출산: 합계출산율 ÷ 25 (가임 기간)
			Double tfr = getStat(stat, "TFR");
			return tfr == null ? base : tfr / 25;
		case "UNEMPLOYMENT": // 실직: 실업률 ÷ 100
			Double unemp = getStat(stat, "UNEMPLOYMENT_RATE");
			return unemp == null ? base : unemp / 100;
		case "NET_MIGRATION": // 이민: 순이민률이 음수(유출)일 때만 반영, 최소 0.2%
			Double mig = getStat(stat, "NET_MIGRATION_RATE");
			return mig == null ? base : Math.max(-mig / 1000, 0.002);
		case "DEATH_CAUSE": // 질병: 기본 확률 × 나이대 배율 × 나라 건강 배율
			Double le = getStat(stat, "LIFE_EXPECTANCY");
			double countryRate = (le == null) ? 1.0 : Math.min(Math.max(75 / le, 0.8), 2.0);
			return base * diseaseAgeRate(age) * countryRate;
		default: // FIXED
			return base;
		}
	}

	// 질병 나이대 배율
	private double diseaseAgeRate(int age) {
		if (age < 5)
			return 2.0;
		if (age < 15)
			return 1.0;
		if (age < 50)
			return 0.8;
		if (age < 70)
			return 1.5;
		return 2.5;
	}

	// 통계 값 꺼내기 (없으면 null → BASE_PROB 사용)
	private Double getStat(Map<String, Object> stat, String key) {
		if (stat == null || stat.get(key) == null)
			return null;
		return ((Number) stat.get(key)).doubleValue();
	}

	// ===== 3단계: 걸린 템플릿 안에서 이벤트 하나 고르기 =====

	public GameEventDTO pickEvent(EventTemplateDTO t, int age, String gender, int wealth, List<String> expIds) {
		List<GameEventDTO> candidates = new ArrayList<>();

		for (GameEventDTO e : eDAO.selectLifeCandidates(t.getTemplateId(), age, gender, wealth)) {
			if (passExp(e, expIds)) {
				candidates.add(e);
			}
		}
		if (candidates.isEmpty())
			return null; // 조건 맞는 이벤트 없음 → 그해는 없던 걸로

		int idx = (int) (Math.random() * candidates.size());
		return candidates.get(idx);
	}

	// 경험 조건 통과 여부
	private boolean passExp(GameEventDTO e, List<String> expIds) {
		// 1. 필요 경험이 있는데 내가 안 겪었으면 탈락 (예: 결혼 안 했는데 첫째 출산)
		if (e.getRequiredExpId() != null && !expIds.contains(e.getRequiredExpId())) {
			return false;
		}
		// 2. 제외 경험을 내가 겪었으면 탈락 (예: 결혼했는데 새로운 만남)
		if (e.getExcludedExpId() != null && expIds.contains(e.getExcludedExpId())) {
			return false;
		}
		// 3. 한 번만 일어나는 이벤트인데 이미 겪었으면 탈락
		if ("Y".equals(e.getIsOnce())) {
			for (String exp : expIds) {
				if (exp.equals(e.getEventId()) || exp.startsWith(e.getEventId() + "_")) {
					return false;
				}
			}
		}
		return true;
	}

	// ===== 4단계: 문장 만들기 + 효과 계산 =====

	// 고른 이벤트 → 결과 (문장 + 효과)
	public EventResultDTO buildResult(EventTemplateDTO t, GameEventDTO e, String name, int age, int year,
			String countryCode) {
		EventResultDTO r = new EventResultDTO();
		r.setEventId(e.getEventId());
		r.setEventType(e.getEventType());
		r.setTitle(e.getTitle());
		r.setAiNarrate("Y".equals(e.getAiNarrate()));
		r.setChoice("Y".equals(e.getIsChoice()));

		// 1. 문장: 템플릿 문장 2~3개 중 하나를 랜덤으로 고르기
		List<String> texts = eDAO.selectTexts(t.getTemplateId());
		String sentence = texts.isEmpty() ? e.getTitle() : texts.get((int) (Math.random() * texts.size()));

		// 2. 변수 치환
		sentence = sentence.replace("{이름}", name).replace("{나이}", String.valueOf(age))
				.replace("{연도}", String.valueOf(year)).replace("{나라}", eDAO.selectCountryName(countryCode))
				.replace("{변수}", e.getVarValue() == null ? "" : e.getVarValue());
		r.setLogText(sentence);

		// 3. 효과 = 템플릿 효과 × 이벤트 배율 (예: 질병 건강 -10 × 결핵 1.5 = -15)
		double scale = e.getEffectScale() == null ? 1.0 : e.getEffectScale();
		r.setEffHealth((int) Math.round(nz(t.getEffHealth()) * scale));
		r.setEffHappiness((int) Math.round(nz(t.getEffHappiness()) * scale));
		r.setEffKnowledge((int) Math.round(nz(t.getEffKnowledge()) * scale));
		r.setEffRelation((int) Math.round(nz(t.getEffRelation()) * scale));
		r.setEffIncomeRate(nz(t.getEffIncomeRate()) * scale);
		r.setEffWealthRate(nz(t.getEffWealthRate()) * scale);
		r.setResultAction(t.getResultAction());

		return r;
	}

	// null이면 0
	private double nz(Number n) {
		return n == null ? 0 : n.doubleValue();
	}
}
