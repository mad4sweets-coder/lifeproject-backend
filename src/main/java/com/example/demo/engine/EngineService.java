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

	// ================= C03 1년 진행 (뼈대) =================
	public LifeDTO nextYear(int lifeId) {

		// 1. 인생 불러오기 (없거나 끝난 인생이면 진행 안 함)
		LifeDTO life = engineDao.selectLife(lifeId);
		if (life == null || !life.getStatus().equals("PLAYING")) {
			return null;
		}

		// 2. 나이 +1
		life.setCurrentAge(life.getCurrentAge() + 1);

		// 3. 앞으로 여기에 순서대로 붙일 자리
		// ① 집중 효과 (C04)
		// ② 능력치 변화 (C05)
		// ③ 학교·직업 (C06)
		// ④ 소득·재산 (C06)
		// ⑤ 인생·역사 이벤트 (C09, C10 - 장윤아님)
		// ⑥ 사망 판정 (C07)

		// 4. 저장 + 그해 기록
		engineDao.updateLifeYear(life);
		engineDao.insertYearLog(life);

		return life;
	}
}
