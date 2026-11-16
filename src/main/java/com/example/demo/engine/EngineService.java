package com.example.demo.engine;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class EngineService {

	private final EngineDao engineDao;

	// 생성자 주입: 스프링이 객체를 만들 때 EngineDao를 넣어줌
	public EngineService(EngineDao engineDao) {
		this.engineDao = engineDao;
	}

	private Random random = new Random();

	// ================= C02 랜덤 출생 =================
	public LifeDTO birth(int decade, String oldGuestToken) {

		// 0. 진행 중이던 비회원 인생이 있으면 포기 처리
		if (oldGuestToken != null && !oldGuestToken.equals("")) {
			engineDao.abandonGuestLife(oldGuestToken);
		}

		// ① 출생 연도: 고른 연대 안에서 랜덤 (1945년 이전 X, 현재 연도 이후 X)
		int minYear = Math.max(decade, 1945);
		int maxYear = Math.min(decade + 9, LocalDate.now().getYear());
		if (minYear > maxYear) {
			return null; // 고를 수 없는 연대
		}
		int birthYear = minYear + random.nextInt(maxYear - minYear + 1);

		// ② 나라: 출생아 수가 많을수록 확률 높게
		List<CountryStatDTO> list = engineDao.selectBirthCountries(decade);
		if (list.isEmpty()) {
			return null; // 그 연대 통계 데이터가 없음
		}

		long total = 0;
		for (CountryStatDTO c : list) {
			total += c.getBirths();
		}

		long pick = (long) (random.nextDouble() * total);
		long sum = 0;
		CountryStatDTO country = list.get(list.size() - 1);
		for (CountryStatDTO c : list) {
			sum += c.getBirths();
			if (pick < sum) {
				country = c;
				break;
			}
		}

		// ③ 성별: 출생 성비 (여아 1당 남아), 없으면 1.05
		double ratio = 1.05;
		if (country.getSexRatioBirth() != null) {
			ratio = country.getSexRatioBirth();
		}
		String gender = random.nextDouble() < ratio / (1 + ratio) ? "M" : "F";

		// ④ 가정형편 1~5, 도시·시골 (도시화율 기준, 없으면 50%)
		int wealthClass = random.nextInt(5) + 1;
		double urbanRate = 50;
		if (country.getUrbanRate() != null) {
			urbanRate = country.getUrbanRate();
		}
		String isUrban = random.nextDouble() * 100 < urbanRate ? "Y" : "N";

		// ⑤ 능력치 시작값: 가정형편 1~5 → 건강·지식 -10 / -5 / 0 / +5 / +10
		int bonus = (wealthClass - 3) * 5;

		LifeDTO life = new LifeDTO();
		life.setBirthYear(birthYear);
		life.setBirthCountry(country.getCountryCode());
		life.setCurrentCountry(country.getCountryCode());
		life.setGender(gender);
		life.setWealthClass(wealthClass);
		life.setIsUrban(isUrban);
		life.setCurrentAge(0);
		life.setStatus("PLAYING");
		life.setHealth(60 + bonus);
		life.setHappiness(55);
		life.setKnowledge(40 + bonus);
		life.setRelation(55);

		// ⑥ 이름 (목록이 비어 있으면 '무명')
		String name = engineDao.selectRandomName(life);
		if (name == null) {
			name = "무명";
		}
		life.setName(name);

		// ⑦ 비회원 임시 토큰 (회원 로그인은 나중에 연결)
		life.setStartType("GUEST");
		life.setGuestToken(UUID.randomUUID().toString());

		// ⑧ 번호 받고 저장 + 0세 기록
		life.setLifeId(engineDao.selectNextLifeId());
		engineDao.insertLife(life);
		engineDao.insertYearLog(life);

		// 출생 화면 표시용
		life.setCountryName(country.getNameKo());
		life.setHistName(engineDao.selectHistName(life));
		life.setLifeExpectancy(country.getLifeExpectancy());
		life.setInfantMortality(country.getInfantMortality());
		life.setGdpPerCapita(country.getGdpPerCapita());

		return life;
	}

	// ================= C03 1년 진행 =================
	public LifeDTO nextYear(int lifeId) {

		// 1. 인생 불러오기 (없거나 끝난 인생이면 진행 안 함)
		LifeDTO life = engineDao.selectLife(lifeId);
		if (life == null || !life.getStatus().equals("PLAYING")) {
			return null;
		}

		// ① 집중 효과 (C04): 고르지 않았으면 나이대 기본값
		if (life.getStageCode() != null && life.getFocusCode() == null) {
			life.setFocusCode(defaultFocus(life.getStageCode()));
		}
		applyFocus(life);

		// ② 능력치 범위 정리 (C05): 0 ~ 100
		fixStats(life);

		// ③ 학교·직업 (C06)
		// ④ 소득·재산 (C06)
		// ⑤ 인생·역사 이벤트 (C09, C10 - 장윤아님)

		// ⑥ 사망 판정 (C07): 올해(현재 나이)를 못 넘기면 사망
		if (isDead(life)) {
			life.setStatus("ENDED");
			life.setDeathAge(life.getCurrentAge());
			life.setDeathCause(deathCause(life.getCurrentAge()));
			engineDao.endLife(life);
			return life;
		}

		// 2. 살아남으면 나이 +1
		life.setCurrentAge(life.getCurrentAge() + 1);

		// 3. 나이대가 바뀌면 집중을 비우고 선택 창 띄우기 (C04)
		String newStage = stageOf(life);
		if (newStage != null && !newStage.equals(life.getStageCode())) {
			life.setStageCode(newStage);
			life.setFocusCode(null);
			life.setNeedFocus(true);
			life.setFocusOptions(focusOptions(newStage));
		}

		// 4. 저장 + 그해 기록
		engineDao.updateLifeYear(life);
		engineDao.insertYearLog(life);

		return life;
	}

	// ================= C04 나이대별 집중 선택 =================

	// 집중 선택 저장 (그 나이대에서 고를 수 있는 것만)
	public LifeDTO chooseFocus(int lifeId, String focusCode) {
		LifeDTO life = engineDao.selectLife(lifeId);
		if (life == null || !life.getStatus().equals("PLAYING") || life.getStageCode() == null) {
			return null;
		}
		if (!focusOptions(life.getStageCode()).contains(focusCode)) {
			return null;
		}
		life.setFocusCode(focusCode);
		engineDao.updateFocus(life);
		return life;
	}

	// 나이대 판정
	// ※ 지금은 나이로만 판정 (6~17 학생, 18~64 무직 성인, 65~ 노년)
	// ※ 대학생(UNIV)·직장인(WORKER)·비학생 청소년(YOUTH)은 C06 진학·직업 붙일 때 추가
	private String stageOf(LifeDTO life) {
		int age = life.getCurrentAge();
		if (age < 6) {
			return null; // 0~5세는 집중 선택 없음
		} else if (age < 18) {
			return "STUDENT";
		} else if (age < 65) {
			return "JOBLESS";
		}
		return "SENIOR";
	}

	// 나이대별로 고를 수 있는 집중 목록
	private List<String> focusOptions(String stage) {
		List<String> list = new ArrayList<>();
		if (stage.equals("STUDENT")) {
			list.add("STUDY"); list.add("PLAY"); list.add("HOUSE"); list.add("SPORT"); list.add("HOBBY");
		} else if (stage.equals("YOUTH")) {
			list.add("HOUSE"); list.add("PLAY"); list.add("SPORT"); list.add("HOBBY");
		} else if (stage.equals("UNIV")) {
			list.add("STUDY"); list.add("PARTTIME"); list.add("JOB_PREP"); list.add("SPORT"); list.add("HOBBY");
		} else if (stage.equals("WORKER")) {
			list.add("WORK"); list.add("BALANCE"); list.add("FAMILY"); list.add("HEALTH"); list.add("SELF_DEV");
		} else if (stage.equals("JOBLESS")) {
			list.add("FAMILY"); list.add("HEALTH"); list.add("SELF_DEV"); list.add("HOBBY"); list.add("REST");
		} else if (stage.equals("SENIOR")) {
			list.add("FAMILY"); list.add("HEALTH"); list.add("BRAIN"); list.add("HOBBY"); list.add("REST");
		}
		return list;
	}

	// 고르지 않았을 때 나이대 기본값
	private String defaultFocus(String stage) {
		if (stage.equals("STUDENT") || stage.equals("UNIV")) {
			return "STUDY";
		} else if (stage.equals("YOUTH")) {
			return "HOUSE";
		} else if (stage.equals("WORKER")) {
			return "BALANCE";
		} else if (stage.equals("JOBLESS")) {
			return "SELF_DEV";
		}
		return "HEALTH"; // SENIOR
	}

	// 집중별 1년 능력치 효과 (건강, 행복, 지식, 관계)
	private void applyFocus(LifeDTO life) {
		String f = life.getFocusCode();
		if (f == null) {
			return; // 0~5세
		}
		double hl = 0, hp = 0, kn = 0, re = 0;
		switch (f) {
		case "STUDY":    kn = 5; hp = -1; re = -1; break;  // 공부
		case "PLAY":     re = 4; hp = 4;           break;  // 친구·놀이
		case "HOUSE":    re = 2; kn = 1; hp = -1;  break;  // 집안 돕기
		case "SPORT":    hl = 4; hp = 2;           break;  // 운동
		case "HOBBY":    hp = 4; kn = 1;           break;  // 취미
		case "PARTTIME": hp = -1; re = 2;          break;  // 아르바이트 (소득은 C06)
		case "JOB_PREP": kn = 3; hp = -2;          break;  // 취업 준비 (준비 점수는 C06)
		case "WORK":     hl = -3; hp = -2; re = -2; break; // 일에 몰두 (승진은 C06)
		case "BALANCE":  hl = 1; hp = 1; re = 1;   break;  // 균형
		case "FAMILY":   re = 4; hp = 3;           break;  // 가족·친구
		case "HEALTH":   hl = 4; hp = 1;           break;  // 건강 관리
		case "SELF_DEV": kn = 3; hp = -1;          break;  // 자기계발
		case "BRAIN":    kn = 2; hl = 1;           break;  // 두뇌 활동
		case "REST":     hp = 3; hl = 2; kn = -1;  break;  // 휴식
		}
		life.setHealth(life.getHealth() + hl);
		life.setHappiness(life.getHappiness() + hp);
		life.setKnowledge(life.getKnowledge() + kn);
		life.setRelation(life.getRelation() + re);
	}

	// ================= C05 능력치 =================

	// 능력치를 0 ~ 100 사이로 맞추기
	private void fixStats(LifeDTO life) {
		life.setHealth(limit(life.getHealth()));
		life.setHappiness(limit(life.getHappiness()));
		life.setKnowledge(limit(life.getKnowledge()));
		life.setRelation(limit(life.getRelation()));
	}

	private double limit(double v) {
		if (v < 0) {
			return 0;
		}
		if (v > 100) {
			return 100;
		}
		return v;
	}

	// ================= C07 사망 판정 =================

	// 올해 죽는지 판정 (true = 사망)
	private boolean isDead(LifeDTO life) {
		double p = deathRate(life);
		return random.nextDouble() < p;
	}

	// 올해 사망 확률 (0 ~ 1)
	private double deathRate(LifeDTO life) {
		int age = life.getCurrentAge();

		// 그해 사는 나라 통계 (없으면 기본값: 기대수명 70, 영아사망률 30‰)
		double lifeExpectancy = 70;
		double infantMortality = 30;
		CountryStatDTO stat = engineDao.selectYearStat(life);
		if (stat != null) {
			if (stat.getLifeExpectancy() != null) {
				lifeExpectancy = stat.getLifeExpectancy();
			}
			if (stat.getInfantMortality() != null) {
				infantMortality = stat.getInfantMortality();
			}
		}

		double p;
		if (age == 0) {
			// 0세: 영아사망률 (‰ → 확률)
			p = infantMortality / 1000;
		} else if (age < 5) {
			// 1~4세: 4년 동안 영아사망률의 30% 정도가 나뉘어 발생
			p = infantMortality / 1000 * 0.3 / 4;
		} else {
			// 5세 이상: 나이가 들수록 빠르게 커지는 곡선
			// 기대수명이 낮은 나라·시대일수록 곡선이 앞당겨짐
			double shift = (75 - lifeExpectancy) * 0.75;
			p = 0.00005 * Math.exp(0.09 * (age + shift));
		}

		// 건강 보정: 건강 50 기준, 낮을수록 위험 (0.5배 ~ 3배)
		double healthRate = 1 + (50 - life.getHealth()) / 50;
		if (healthRate < 0.5) {
			healthRate = 0.5;
		}
		if (healthRate > 3) {
			healthRate = 3;
		}
		p = p * healthRate;

		// 110세 이상은 무조건 사망, 확률은 0 ~ 1 사이로
		if (age >= 110 || p > 1) {
			p = 1;
		}
		return p;
	}

	// 사망 원인 (권역 사망 원인 데이터가 들어오기 전 임시)
	private String deathCause(int age) {
		if (age < 5) {
			return "감염병";
		} else if (age < 15) {
			return "사고";
		} else if (age < 50) {
			return random.nextBoolean() ? "사고" : "질병";
		} else if (age < 70) {
			return "질병";
		}
		return "노환";
	}
}
