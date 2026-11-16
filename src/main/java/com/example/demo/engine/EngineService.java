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

		// 0. 오래된 비회원 인생 정리 (C03): 종료 후 1일 또는 7일 미접속
		//    1) 통계용 숫자만 옮기고 → 2) 삭제 (1이 실패하면 에러로 멈춰서 2는 실행 안 됨)
		engineDao.moveOldGuestStats();
		engineDao.deleteOldGuestLives();

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
		life.setEduLevel("NONE");
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

		// 그해 사는 나라 통계 (사망 판정·진학에 사용)
		CountryStatDTO stat = engineDao.selectYearStat(life);

		// ① 집중 효과 (C04): 고르지 않았으면 나이대 기본값
		if (life.getStageCode() != null && life.getFocusCode() == null) {
			life.setFocusCode(defaultFocus(life.getStageCode()));
		}
		applyFocus(life);

		// ② 능력치 자연 변화 + 범위 정리 (C05)
		naturalChange(life);
		fixStats(life);

		// ④ 소득·재산 (C06-2)
		moneyStep(life, stat);

		// ⑤ 인생·역사 이벤트 (C09, C10 - 장윤아님)

		// ⑥ 사망 판정 (C07): 올해(현재 나이)를 못 넘기면 사망
		if (isDead(life, stat)) {
			engineDao.updateLifeYear(life); // 마지막 해의 능력치·소득·재산 저장
			life.setStatus("ENDED");
			life.setDeathAge(life.getCurrentAge());
			life.setDeathCause(deathCause(life));
			finalResult(life);
			engineDao.endLife(life);
			return life;
		}

		// 2. 살아남으면 나이 +1
		life.setCurrentAge(life.getCurrentAge() + 1);

		// ③ 학교: 새 나이 기준으로 졸업·중퇴·진학 판정 (C06-1)
		schoolStep(life, stat);

		// ③ 직업: 정년 은퇴 판정, 일자리가 없으면 직업 3개 제안 (C06-2)
		jobStep(life, stat);

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

	// ================= C06-2 직업 선택 =================

	// 제안받은 직업 중 하나를 선택 (필요 학력을 갖춘 직업만)
	public LifeDTO chooseJob(int lifeId, String jobId) {
		LifeDTO life = engineDao.selectLife(lifeId);
		if (life == null || !life.getStatus().equals("PLAYING")) {
			return null;
		}
		if (life.getCurrentAge() < 15 || "ENROLLED".equals(life.getSchoolStatus())
				|| "EMPLOYED".equals(life.getJobStatus()) || "RETIRED".equals(life.getJobStatus())) {
			return null; // 지금은 직업을 고를 수 없는 상태
		}
		JobDTO job = engineDao.selectJob(jobId);
		if (job == null || levelIndex(job.getRequiredEdu()) > finishedLevel(life)) {
			return null; // 없는 직업이거나 학력이 부족
		}

		life.setJobId(job.getJobId());
		life.setJobStatus("EMPLOYED");
		life.setJobYears(0);
		life.setIncomeRate(job.getIncomeRate());
		life.setPrepScore(0);
		addExperience(life, job.getJobId()); // 직업 경험 (C05)

		// 직장인이 됐으니 나이대 변경 → 집중 다시 선택
		String newStage = stageOf(life);
		if (newStage != null && !newStage.equals(life.getStageCode())) {
			life.setStageCode(newStage);
			life.setFocusCode(null);
			life.setNeedFocus(true);
			life.setFocusOptions(focusOptions(newStage));
		}
		engineDao.updateLifeYear(life);
		life.setJobName(job.getJobName());
		return life;
	}

	// 나이대 판정
	private String stageOf(LifeDTO life) {
		int age = life.getCurrentAge();
		String level = life.getEduLevel();
		boolean enrolled = "ENROLLED".equals(life.getSchoolStatus());

		if (age < 6) {
			return null; // 0~5세는 집중 선택 없음
		}
		if (enrolled && (level.equals("UNI") || level.equals("GRAD"))) {
			return "UNIV"; // 대학생
		}
		if (enrolled) {
			return "STUDENT"; // 초·중·고 학생
		}
		if ("RETIRED".equals(life.getJobStatus()) || age >= 65 && !"EMPLOYED".equals(life.getJobStatus())) {
			return "SENIOR"; // 노년 (65세 이상 또는 은퇴)
		}
		if ("EMPLOYED".equals(life.getJobStatus())) {
			return "WORKER"; // 직장인 (15세 이상, 직업 있음)
		}
		if (age < 18) {
			return "YOUTH"; // 학교 안 다니는 청소년
		}
		if (age < 65) {
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

	// 자연 변화: 지식은 매년 -1 (공부를 멈추면 또래에 밀림), 건강은 50세부터 매년 -1
	private void naturalChange(LifeDTO life) {
		if (life.getCurrentAge() >= 6) {
			life.setKnowledge(life.getKnowledge() - 1);
		}
		if (life.getCurrentAge() >= 50) {
			life.setHealth(life.getHealth() - 1);
		}
	}

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

	// ================= C06-1 학교 (진학·졸업·중퇴) =================

	// 학교 단계: 초등 6년 → 중등 3년 → 고등 3년 → 대학 4년 → 대학원 2년 (6세 입학, 모든 나라 같은 기간)
	private String[] levels = { "ELEM", "MIDDLE", "HIGH", "UNI", "GRAD" };
	private int[] startAges = { 6, 12, 15, 18, 22 };
	private int[] years = { 6, 3, 3, 4, 2 };

	// 학력 코드 → 몇 번째 단계인지 (NONE은 -1)
	private int levelIndex(String level) {
		for (int i = 0; i < levels.length; i++) {
			if (levels[i].equals(level)) {
				return i;
			}
		}
		return -1;
	}

	// 1년 학교 처리 (새 나이 기준)
	private void schoolStep(LifeDTO life, CountryStatDTO stat) {
		int age = life.getCurrentAge();
		int idx = levelIndex(life.getEduLevel());
		String status = life.getSchoolStatus();

		// 1. 다니는 중이면: 졸업 나이가 됐는지, 아니면 중퇴하는지
		if ("ENROLLED".equals(status)) {
			if (age >= startAges[idx] + years[idx]) {
				life.setSchoolStatus("GRADUATED");
				addExperience(life, "EDU_" + levels[idx]); // 졸업 경험 (C05)
				status = "GRADUATED";
			} else {
				if (random.nextDouble() < dropRate(life, idx)) {
					life.setSchoolStatus("DROPPED");
					if (life.getWealthClass() <= 2) {
						life.setDropReason("집안 형편이 어려워 학교를 그만둠");
					} else {
						life.setDropReason("성적이 떨어져 학교를 그만둠");
					}
				}
				return;
			}
		}

		// 2. 진학: 아직 입학 전(6세)이거나, 졸업한 해에 다음 단계 입학 나이면 판정
		int next = idx + 1;
		boolean canEnter = (idx == -1 && status == null) || "GRADUATED".equals(status);
		if (canEnter && next < levels.length && age == startAges[next]) {
			if (random.nextDouble() < enrollRate(life, stat, next)) {
				life.setEduLevel(levels[next]);
				life.setSchoolStatus("ENROLLED");
				life.setDropReason(null);
			}
		}
	}

	// 진학 확률 = 권역 진학률 × 가정형편 보정 × 도시/시골 보정
	private double enrollRate(LifeDTO life, CountryStatDTO stat, int next) {

		// 권역 진학률 (%) - 데이터가 없으면 기본값
		double elem = 80, middle = 60, high = 40, uni = 15;
		RegionStatDTO rs = engineDao.selectRegionStat(life);
		if (rs != null) {
			if (rs.getEnrollElem() != null) {
				elem = rs.getEnrollElem();
			}
			if (rs.getEnrollMiddle() != null) {
				middle = rs.getEnrollMiddle();
			}
			if (rs.getEnrollHigh() != null) {
				high = rs.getEnrollHigh();
			}
			if (rs.getEnrollUni() != null) {
				uni = rs.getEnrollUni();
			}
		}

		// 진학률은 "또래 전체 중 몇 %"라서, 앞 단계를 마친 사람 기준으로 바꿔서 계산
		double p;
		if (next == 0) {
			p = elem / 100;
		} else if (next == 1) {
			p = middle / Math.max(elem, 1);
		} else if (next == 2) {
			p = high / Math.max(middle, 1);
		} else if (next == 3) {
			p = uni / Math.max(high, 1);
		} else {
			p = uni / 100 * 0.3; // 대학원: 대학 졸업자 중 대학 진학률 × 0.3
		}
		if (p > 0.98) {
			p = 0.98;
		}

		// 가정형편 보정: e^(d × (형편 - 3)), d = 1인당 GDP 15,000달러 미만 0.6, 이상 0.45
		double gdp = 5000;
		if (stat != null && stat.getGdpPerCapita() != null) {
			gdp = stat.getGdpPerCapita();
		}
		double d = gdp < 15000 ? 0.6 : 0.45;
		p = p * Math.exp(d * (life.getWealthClass() - 3));

		// 도시/시골 보정 (1인당 GDP가 낮은 나라만): 도시 ×1.4, 시골 ×0.75
		if (gdp < 5000) {
			if (life.getIsUrban().equals("Y")) {
				p = p * 1.4;
			} else {
				p = p * 0.75;
			}
		}

		if (p > 0.99) {
			p = 0.99;
		}
		return p;
	}

	// 1년 중퇴 확률
	private double dropRate(LifeDTO life, int idx) {
		double p = 0.01;
		// 지식 45 미만, 가정형편 1~2분위면 중퇴 확률 증가
		if (life.getKnowledge() < 45) {
			p = p * 2;
		}
		if (life.getWealthClass() <= 2) {
			p = p * 2;
		}
		// 대학·대학원은 지식 배율 (85 이상 0.45 / 70 이상 0.78 / 50 이상 1.12 / 50 미만 1.68)
		if (idx >= 3) {
			double k = life.getKnowledge();
			if (k >= 85) {
				p = p * 0.45;
			} else if (k >= 70) {
				p = p * 0.78;
			} else if (k >= 50) {
				p = p * 1.12;
			} else {
				p = p * 1.68;
			}
		}
		return p;
	}

	// ================= C06-2 직업·소득·재산 =================

	// 마친 학력 단계 (졸업했으면 그 단계, 재학·중퇴면 그 전 단계, 없으면 -1)
	private int finishedLevel(LifeDTO life) {
		int idx = levelIndex(life.getEduLevel());
		if ("GRADUATED".equals(life.getSchoolStatus())) {
			return idx;
		}
		return idx - 1;
	}

	// 은퇴 나이: 고소득국(1인당 GDP 15,000달러 이상) 65세, 중앙아시아 62세, 그 외 60세
	private int retireAge(CountryStatDTO stat) {
		if (stat != null && stat.getGdpPerCapita() != null && stat.getGdpPerCapita() >= 15000) {
			return 65;
		}
		if (stat != null && "CENTRAL".equals(stat.getRegionCode())) {
			return 62;
		}
		return 60;
	}

	// 1년 직업 처리 (새 나이 기준)
	private void jobStep(LifeDTO life, CountryStatDTO stat) {
		int age = life.getCurrentAge();

		// 1. 정년 은퇴: 연금 = 마지막 소득 × 0.3
		if ("EMPLOYED".equals(life.getJobStatus()) && age >= retireAge(stat)) {
			life.setJobStatus("RETIRED");
			life.setPension(Math.round(life.getIncome() * 0.3));
			return;
		}

		// 2. 15세 이상, 학교 안 다니고, 일자리가 없으면 매년 직업 3개 제안
		boolean noJob = life.getJobStatus() == null || "UNEMPLOYED".equals(life.getJobStatus());
		if (age >= 15 && !"ENROLLED".equals(life.getSchoolStatus()) && noJob && age < retireAge(stat)) {
			List<JobDTO> offers = offerJobs(life);
			if (!offers.isEmpty()) {
				life.setNeedJob(true);
				life.setJobOffers(offers);
			}
		}
	}

	// 필요 학력을 갖춘 직업 중 권역 산업 비율에 따라 3개 제안
	// 취업 준비 점수(0~2)가 높을수록 소득 배율 높은 직업이 나올 확률 증가
	private List<JobDTO> offerJobs(LifeDTO life) {
		// 권역 산업 비율 (%) - 데이터가 없으면 기본값
		double agri = 40, industry = 25, service = 35;
		RegionStatDTO rs = engineDao.selectRegionStat(life);
		if (rs != null) {
			if (rs.getAgriRate() != null) {
				agri = rs.getAgriRate();
			}
			if (rs.getIndustryRate() != null) {
				industry = rs.getIndustryRate();
			}
			if (rs.getServiceRate() != null) {
				service = rs.getServiceRate();
			}
		}

		// 고를 수 있는 직업과 뽑힐 무게
		List<JobDTO> pool = new ArrayList<>();
		List<Double> weights = new ArrayList<>();
		int finished = finishedLevel(life);
		for (JobDTO job : engineDao.selectJobs()) {
			if (levelIndex(job.getRequiredEdu()) > finished) {
				continue; // 학력 부족
			}
			double w = service;
			if (job.getIndustry().equals("AGRI")) {
				w = agri;
			} else if (job.getIndustry().equals("INDUSTRY")) {
				w = industry;
			}
			w = w * (1 + 0.5 * life.getPrepScore() * (job.getIncomeRate() - 1));
			if (w < 0.1) {
				w = 0.1;
			}
			pool.add(job);
			weights.add(w);
		}

		// 무게 비율대로 겹치지 않게 3개 뽑기
		List<JobDTO> offers = new ArrayList<>();
		while (offers.size() < 3 && !pool.isEmpty()) {
			double total = 0;
			for (double w : weights) {
				total += w;
			}
			double pick = random.nextDouble() * total;
			double sum = 0;
			int chosen = pool.size() - 1;
			for (int i = 0; i < pool.size(); i++) {
				sum += weights.get(i);
				if (pick < sum) {
					chosen = i;
					break;
				}
			}
			offers.add(pool.remove(chosen));
			weights.remove(chosen);
		}
		return offers;
	}

	// 1년 소득·재산 처리
	private void moneyStep(LifeDTO life, CountryStatDTO stat) {
		double gdp = 5000;
		if (stat != null && stat.getGdpPerCapita() != null) {
			gdp = stat.getGdpPerCapita();
		}
		int age = life.getCurrentAge();
		String focus = life.getFocusCode();

		// 취업 준비 집중: 1년마다 준비 점수 +1 (최대 2)
		if ("JOB_PREP".equals(focus) && life.getPrepScore() < 2) {
			life.setPrepScore(life.getPrepScore() + 1);
		}

		// 1. 소득
		double income = 0;
		if ("EMPLOYED".equals(life.getJobStatus())) {
			// 소득 = 1인당 GDP × 소득 배율 × 랜덤(±20%)
			income = gdp * life.getIncomeRate() * (0.8 + random.nextDouble() * 0.4);

			// 소득 배율 상승: 근속 10년까지 +1.5%, 20년까지 +0.7%, 그 후 +0.1%, 55세 이후 -0.4%
			int y = life.getJobYears();
			double up = 0.1;
			if (y < 10) {
				up = 1.5;
			} else if (y < 20) {
				up = 0.7;
			}
			if (age >= 55) {
				up = up - 0.4;
			}
			if ("WORK".equals(focus)) {
				up = up + 1.2; // 일에 몰두
			} else if ("SELF_DEV".equals(focus)) {
				up = up + 0.6; // 자기계발
			}
			life.setIncomeRate(life.getIncomeRate() * (1 + up / 100));
			life.setJobYears(y + 1);
		} else if ("RETIRED".equals(life.getJobStatus()) && life.getPension() != null) {
			income = life.getPension(); // 연금
		} else if ("PARTTIME".equals(focus)) {
			income = gdp * 0.15; // 아르바이트
		}
		life.setIncome(Math.round(income));

		// 2. 생활비 (18세 미만·학생은 제외)
		double cost = 0;
		if (age >= 18 && !"ENROLLED".equals(life.getSchoolStatus())) {
			if (income > 0) {
				// 저축률 0~20%: 나라 평균 소득(1인당 GDP)보다 높을수록 커짐
				double saveRate = (income / gdp - 1) * 0.1;
				if (saveRate < 0) {
					saveRate = 0;
				}
				if (saveRate > 0.2) {
					saveRate = 0.2;
				}
				cost = income * (1 - saveRate);
			} else {
				cost = gdp * 0.3;
			}
		}

		// 3. 남은 돈은 재산에 (마이너스면 빚)
		life.setAssets(life.getAssets() + Math.round(income - cost));
	}

	// ================= C05 경험 기록 =================
	private void addExperience(LifeDTO life, String expId) {
		ExperienceDTO exp = new ExperienceDTO();
		exp.setLifeId(life.getLifeId());
		exp.setExpId(expId);
		exp.setAge(life.getCurrentAge());
		exp.setYearNo(life.getBirthYear() + life.getCurrentAge());
		engineDao.insertExperience(exp);
	}

	// ================= C07 사망 판정 =================

	// 올해 죽는지 판정 (true = 사망)
	private boolean isDead(LifeDTO life, CountryStatDTO stat) {
		double p = deathRate(life, stat);
		return random.nextDouble() < p;
	}

	// 올해 사망 확률 (0 ~ 1)
	private double deathRate(LifeDTO life, CountryStatDTO stat) {
		int age = life.getCurrentAge();

		// 그해 사는 나라 통계 (없으면 기본값: 기대수명 70, 영아사망률 30‰)
		double lifeExpectancy = 70;
		double infantMortality = 30;
		String region = "";
		if (stat != null) {
			if (stat.getLifeExpectancy() != null) {
				lifeExpectancy = stat.getLifeExpectancy();
			}
			if (stat.getInfantMortality() != null) {
				infantMortality = stat.getInfantMortality();
			}
			if (stat.getRegionCode() != null) {
				region = stat.getRegionCode();
			}
		}

		// 1. 나이별 기본 확률
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

		// 2. 성별: 남자 ×1.08, 여자 ×0.92
		if (life.getGender().equals("M")) {
			p = p * 1.08;
		} else {
			p = p * 0.92;
		}

		// 3. 그해 사망 배율 (건강 × 가정형편)
		// 건강: 기준 건강(49세까지 60, 50세부터 매년 -1)보다 낮으면 위험
		double baseHealth = 60;
		if (age >= 50) {
			baseHealth = 60 - (age - 50);
		}
		double k = 0.03;
		if (age < 5) {
			k = 0.02;
		}
		double healthRate = Math.exp((baseHealth - life.getHealth()) * k);
		if (healthRate < 0.5) {
			healthRate = 0.5;
		}

		// 가정형편: 형편이 좋을수록 위험 감소 (남아시아 0.25, 서아시아 0.15, 그 외 0.2)
		double r = 0.2;
		if (region.equals("SOUTH")) {
			r = 0.25;
		} else if (region.equals("WEST")) {
			r = 0.15;
		}
		double wealthRate = Math.exp(-r * (life.getWealthClass() - 3));

		double rate = healthRate * wealthRate;
		if (rate < 0.25) {
			rate = 0.25;
		}
		if (rate > 9) {
			rate = 9;
		}

		// 85세 이상은 배율 효과를 점점 줄임 (100세에 효과 없음)
		if (age >= 85) {
			double weaken = (100 - age) / 15.0;
			if (weaken < 0) {
				weaken = 0;
			}
			rate = 1 + (rate - 1) * weaken;
		}
		p = p * rate;

		// 110세 이상은 무조건 사망, 확률은 0 ~ 1 사이로
		if (age >= 110 || p > 1) {
			p = 1;
		}
		return p;
	}

	// 사망 원인: 권역·나이대별 사망 원인 비율로 결정 (데이터가 없으면 임시 원인)
	private String deathCause(LifeDTO life) {
		int age = life.getCurrentAge();
		String group = "70+";
		if (age < 5) {
			group = "0-4";
		} else if (age < 15) {
			group = "5-14";
		} else if (age < 50) {
			group = "15-49";
		} else if (age < 70) {
			group = "50-69";
		}

		DeathCauseDTO cond = new DeathCauseDTO();
		cond.setCountryCode(life.getCurrentCountry());
		cond.setAgeGroup(group);
		cond.setYearNo(life.getBirthYear() + age);
		List<DeathCauseDTO> list = engineDao.selectDeathCauses(cond);

		if (!list.isEmpty()) {
			// 비율대로 하나 뽑기
			double total = 0;
			for (DeathCauseDTO d : list) {
				total += d.getRatio();
			}
			double pick = random.nextDouble() * total;
			double sum = 0;
			for (DeathCauseDTO d : list) {
				sum += d.getRatio();
				if (pick < sum) {
					return d.getCauseName();
				}
			}
			return list.get(list.size() - 1).getCauseName();
		}

		// 데이터가 없을 때 임시 원인
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

	// 최종 결과값 (C07): 평균 소득, 인생 점수
	// 인생 점수 = 마지막 능력치 평균 + 수명 10년당 1점 + 대학 졸업 5점 / 대학원 졸업 8점
	private void finalResult(LifeDTO life) {
		Long avg = engineDao.selectAvgIncome(life.getLifeId());
		if (avg == null) {
			avg = 0L;
		}
		life.setAvgIncome(avg);

		double score = (life.getHealth() + life.getHappiness() + life.getKnowledge() + life.getRelation()) / 4;
		score = score + life.getDeathAge() / 10;
		if ("GRADUATED".equals(life.getSchoolStatus())) {
			if (life.getEduLevel().equals("UNI")) {
				score = score + 5;
			} else if (life.getEduLevel().equals("GRAD")) {
				score = score + 8;
			}
		} else if (life.getEduLevel().equals("GRAD")) {
			score = score + 5; // 대학원 재학·중퇴여도 대학은 졸업
		}
		life.setLifeScore(Math.round(score * 10) / 10.0);
	}
}
