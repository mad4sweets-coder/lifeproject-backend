package com.example.demo.engine;

import java.time.LocalDate;
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

		// 2. 앞으로 여기에 순서대로 붙일 자리
		// ① 집중 효과 (C04)
		// ② 능력치 변화 (C05)
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

		// 3. 살아남으면 나이 +1, 저장 + 그해 기록
		life.setCurrentAge(life.getCurrentAge() + 1);
		engineDao.updateLifeYear(life);
		engineDao.insertYearLog(life);

		return life;
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
