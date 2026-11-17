package com.example.demo.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.engine.LifeDTO;
import com.example.demo.event.dto.EventChoiceDTO;
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
		case "TFR": // 출산: 합계출산율 ÷ 출산 가능 기간 (템플릿 나이 범위)
			Double tfr = getStat(stat, "TFR");
			int years = t.getMaxAge() - t.getMinAge() + 1;
			return tfr == null ? base : tfr / years;
		case "UNEMPLOYMENT": // 실직: 실업률 ÷ 100
			Double unemp = getStat(stat, "UNEMPLOYMENT_RATE");
			return unemp == null ? base : unemp / 100;
		case "NET_MIGRATION": // 이민: 기본 확률 × (1 - 순이민률‰), 배율 0.5~3.0배 제한 (유출 많을수록 ↑)
			Double mig = getStat(stat, "NET_MIGRATION_RATE");
			if (mig == null)
				return base; // 통계 없으면 기본 확률
			double migRate = Math.min(Math.max(1 - mig, 0.5), 3.0); // 바닥 0.5: 유입국도 최소 절반 / 천장 3.0: 극단값 방지
			return base * migRate;
		case "DEATH_CAUSE": // 질병: 기본 확률 × 나이대 배율 × 나라 건강 배율
			Double le = getStat(stat, "LIFE_EXPECTANCY");
			// 나라 건강 배율 = 75(기준 기대수명) ÷ 기대수명, 0.8~2.0배 제한 / 통계 없으면 1.0
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
		r.setExpId(e.getEventId());
		r.setEventType(e.getEventType());
		r.setTitle(e.getTitle());
		r.setAiNarrate("Y".equals(e.getAiNarrate()));
		r.setChoice("Y".equals(e.getIsChoice()));
		if (r.isChoice()) {
			r.setChoices(eDAO.selectChoices(e.getEventId()));
		}

		// 1. 문장: 템플릿 문장 2~3개 중 하나를 랜덤으로 고르기
		List<String> texts = eDAO.selectTexts(t.getTemplateId());
		String sentence = texts.isEmpty() ? e.getTitle() : texts.get((int) (Math.random() * texts.size()));

		// 2. 변수 치환
		String countryName = eDAO.selectCountryName(countryCode);
		sentence = sentence.replace("{이름}", name).replace("{나이}", String.valueOf(age))
				.replace("{연도}", String.valueOf(year)).replace("{나라}", countryName == null ? countryCode : countryName)
				.replace("{변수}", e.getVarValue() == null ? "" : e.getVarValue());
		r.setLogText(sentence);

		// 3. 효과 = 템플릿 효과 × 이벤트 배율 (예: 질병 건강 -10 × 결핵 1.5 = -15)
		double scale = e.getEffectScale() == null ? 1.0 : e.getEffectScale();
		r.setEffHealth((int) Math.round(nz(t.getEffHealth()) * scale));
		r.setEffHappiness((int) Math.round(nz(t.getEffHappiness()) * scale));
		r.setEffKnowledge((int) Math.round(nz(t.getEffKnowledge()) * scale));
		r.setEffRelation((int) Math.round(nz(t.getEffRelation()) * scale));

		// 자산: 이벤트에 값이 있으면 그 값 그대로(소득 변화 0), 없으면 템플릿 × 배율
		if (e.getEffWealthRate() != null) {
			r.setEffWealthRate(round3(e.getEffWealthRate()));
			r.setEffIncomeRate(0);
		} else {
			r.setEffIncomeRate(round3(nz(t.getEffIncomeRate()) * scale));
			r.setEffWealthRate(round3(nz(t.getEffWealthRate()) * scale));
		}
		r.setResultAction(t.getResultAction());

		return r;
	}

	// null이면 0
	private double nz(Number n) {
		return n == null ? 0 : n.doubleValue();
	}

	// 소수점 3자리 반올림 (0.30000000000000004 → 0.3)
	private double round3(double v) {
		return Math.round(v * 1000) / 1000.0;
	}

	// ===== 엔진 연결: 1년치 이벤트 굴리기 (EngineService가 호출) =====
	public List<EventResultDTO> rollYear(LifeDTO life) {
		int age = life.getCurrentAge();
		int year = life.getBirthYear() + age;
		List<String> expIds = eDAO.selectExpIds(life.getLifeId());
		List<EventResultDTO> results = new ArrayList<>();
		boolean hasChoice = false; // 한 해에 선택 이벤트는 1개만

		for (EventTemplateDTO t : rollTemplates(age, year, life.getCurrentCountry())) {
			GameEventDTO e = pickEvent(t, age, life.getGender(), life.getWealthClass(), expIds);
			if (e == null)
				continue; // 조건 맞는 이벤트 없음
			if ("Y".equals(e.getIsChoice())) {
				if (hasChoice)
					continue; // 이미 선택 이벤트가 있으면 건너뜀
				hasChoice = true;
			}

			results.add(buildResult(t, e, life.getName(), age, year, life.getCurrentCountry()));
		}
		return results;
	}

	// ===== 5단계: 선택지를 고르면 결과 만들기 =====

	public EventResultDTO applyChoice(LifeDTO life, int choiceId, int age) {
		EventChoiceDTO c = eDAO.selectChoice(choiceId);
		if (c == null)
			return null; // 없는 선택지

		int year = life.getBirthYear() + age;

		EventResultDTO r = new EventResultDTO();
		r.setEventId(c.getEventId());
		r.setExpId(c.getEventId() + "_" + c.getChoiceOrder()); // 예: LE_MIGRATE_WORK_1
		r.setEventType("LIFE");
		r.setTitle(c.getChoiceText());

		// 1. 결과 문구 변수 치환
		String countryName = eDAO.selectCountryName(life.getCurrentCountry());
		String text = c.getResultText().replace("{이름}", life.getName()).replace("{나이}", String.valueOf(age))
				.replace("{연도}", String.valueOf(year))
				.replace("{나라}", countryName == null ? life.getCurrentCountry() : countryName);

		// 2. 효과 (선택지 값 그대로)
		r.setEffHealth((int) nz(c.getEffHealth()));
		r.setEffHappiness((int) nz(c.getEffHappiness()));
		r.setEffKnowledge((int) nz(c.getEffKnowledge()));
		r.setEffRelation((int) nz(c.getEffRelation()));
		r.setEffIncomeRate(round3(nz(c.getEffIncomeRate())));
		double wealth = round3(nz(c.getEffWealthRate()));

		// 3. 투자: 50% 확률로 성공(자산 +) / 실패(자산 −)
		if ("INVEST".equals(c.getResultAction())) {
			if (Math.random() < 0.5) {
				text = text + " 투자는 성공적이었다.";
			} else {
				wealth = -wealth;
				text = text + " 하지만 투자는 실패해 손해를 봤다.";
				r.setEffIncomeRate(0); // 실패하면 소득 증가 없음
				r.setEffHappiness(-Math.abs(r.getEffHappiness()) - 3); // 실패하면 행복 감소
			}
			r.setResultAction(null); // 투자는 엔진에서 따로 처리할 상태 변화 없음
		} else {
			r.setResultAction(c.getResultAction()); // MARRY, MIGRATE 등
		}
		r.setEffWealthRate(wealth);
		r.setMigrateTo(c.getMigrateTo());
		r.setLogText(text);

		return r;
	}
}
