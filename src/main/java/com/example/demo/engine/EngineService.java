package com.example.demo.engine;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.demo.event.EventDAO;
import com.example.demo.event.EventService;
import com.example.demo.event.dto.EventResultDTO;

@Service
public class EngineService {

	private final EngineDao engineDao;
	private final EventService eventService; // C09·C10 이벤트 (장윤아님)
	private final EventDAO eventDao; // 선택지 다시 불러오기용 (장윤아님, 읽기만)

	// 생성자 주입: 스프링이 객체를 만들 때 EngineDao, EventService, EventDAO를 넣어줌
	public EngineService(EngineDao engineDao, EventService eventService, EventDAO eventDao) {
		this.engineDao = engineDao;
		this.eventService = eventService;
		this.eventDao = eventDao;
	}

	private Random random = new Random();

	// ================= C02 랜덤 출생 =================
	// memberId: 로그인한 회원 번호 (비회원이면 null)
	public LifeDTO birth(int decade, String oldGuestToken, Integer memberId) {

		// 0. 진행 중이던 인생이 있으면 포기 처리 (회원 1명·토큰 1개당 진행 중 인생 1개)
		if (memberId != null) {
			engineDao.abandonMemberLife(memberId);
		} else if (oldGuestToken != null && !oldGuestToken.equals("")) {
			engineDao.abandonGuestLife(oldGuestToken);
		}

		// 0. 오래된 비회원 인생 정리 (C03): 종료 후 1일 또는 7일 미접속
		//    1) 통계용 숫자만 옮기고 → 2) 삭제 (1이 실패하면 에러로 멈춰서 2는 실행 안 됨)
		engineDao.moveOldGuestStats();
		engineDao.deleteOldGuestLives();

		// ① 출생 연도: 고른 연대 안에서 랜덤 (1940년대 ~ 현재, 현재 연도 이후 X)
		int minYear = decade;
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

		// ⑦ 회원이면 회원 번호, 비회원이면 새 임시 토큰 발급
		if (memberId != null) {
			life.setStartType("MEMBER");
			life.setMemberId(memberId);
		} else {
			life.setStartType("GUEST");
			life.setGuestToken(UUID.randomUUID().toString());
		}

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

	// ================= 본인 확인 =================

	// 내 인생인지 확인: 회원 인생은 로그인한 회원 번호, 비회원 인생은 임시 토큰이 맞아야 함
	public boolean isMyLife(int lifeId, Integer memberId, String guestToken) {
		LifeDTO life = engineDao.selectLife(lifeId);
		if (life == null) {
			return false;
		}
		if (life.getMemberId() != null) {
			return life.getMemberId().equals(memberId);
		}
		return life.getGuestToken() != null && life.getGuestToken().equals(guestToken);
	}

	// ================= C03 이어하기 =================

	// 진행 중인 인생 찾기 (회원은 회원 번호, 비회원은 임시 토큰)
	public LifeDTO currentLife(Integer memberId, String guestToken) {
		if (memberId == null && (guestToken == null || guestToken.equals(""))) {
			return null;
		}
		LifeDTO cond = new LifeDTO();
		cond.setMemberId(memberId);
		cond.setGuestToken(guestToken);
		LifeDTO life = engineDao.selectPlayingLife(cond);
		if (life != null && life.getStageCode() != null && life.getFocusCode() == null) {
			// 집중을 아직 안 골랐으면 선택 창 다시 띄우기
			life.setNeedFocus(true);
			life.setFocusOptions(focusOptions(life.getStageCode(), life));
		}
		restoreChoice(life); // 선택 이벤트를 아직 안 골랐으면 선택 창 다시 띄우기
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
			life.setFocusCode(defaultFocus(life));
		}
		applyFocus(life);
		fixStats(life);

		// ② 능력치 자연 변화 (C05): 건강·관계·행복·지식이 목표값으로 조금씩 이동
		naturalChange(life);
		fixStats(life);

		// ④ 소득·재산 (C06-2)
		moneyStep(life, stat);

		// ⑤ 인생·역사 이벤트 (C09, C10): 윤아님 rollYear로 올해 일어난 일을 뽑고 → 효과·상태를 인생에 반영
		eventStep(life);

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
			life.setFocusOptions(focusOptions(newStage, life));
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
		if (!focusOptions(life.getStageCode(), life).contains(focusCode)) {
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
			life.setFocusOptions(focusOptions(newStage, life));
		}
		engineDao.updateLifeYear(life);
		life.setJobName(job.getJobName());
		return life;
	}

	// 나이대 판정 (원작 기준)
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
		if (age >= 65 || "RETIRED".equals(life.getJobStatus())) {
			return "SENIOR"; // 노년 (65세 이상 또는 은퇴, 일하고 있으면 '일에 몰두' 추가)
		}
		if ("EMPLOYED".equals(life.getJobStatus())) {
			return "WORKER"; // 직장인
		}
		if (age < 15) {
			return "YOUTH"; // 학교 안 다니는 아이 (6~14세)
		}
		return "JOBLESS"; // 일이 없는 15세 이상
	}

	// 나이대별로 고를 수 있는 집중 목록 (원작 기준)
	private List<String> focusOptions(String stage, LifeDTO life) {
		List<String> list = new ArrayList<>();
		if (stage.equals("STUDENT")) {
			list.add("STUDY"); list.add("PLAY"); list.add("HOUSE"); list.add("SPORT"); list.add("HOBBY");
		} else if (stage.equals("YOUTH")) {
			list.add("HOUSE"); list.add("PLAY"); list.add("SPORT"); list.add("HOBBY");
		} else if (stage.equals("UNIV")) {
			list.add("STUDY"); list.add("PLAY"); list.add("PARTTIME"); list.add("JOB_PREP"); list.add("SPORT"); list.add("HOBBY");
		} else if (stage.equals("WORKER")) {
			list.add("WORK"); list.add("BALANCE"); list.add("FAMILY"); list.add("HEALTH"); list.add("SELF_DEV"); list.add("HOBBY");
		} else if (stage.equals("JOBLESS")) {
			list.add("FAMILY"); list.add("HEALTH"); list.add("SELF_DEV"); list.add("HOBBY"); list.add("REST");
		} else if (stage.equals("SENIOR")) {
			if ("EMPLOYED".equals(life.getJobStatus())) {
				list.add("WORK"); list.add("FAMILY"); list.add("HEALTH"); list.add("BRAIN"); list.add("HOBBY");
			} else {
				list.add("FAMILY"); list.add("HEALTH"); list.add("BRAIN"); list.add("HOBBY"); list.add("REST");
			}
		}
		return list;
	}

	// 고르지 않았을 때 기본값 (원작 기준): 목록에 '균형'이 있으면 균형, 없으면 목록 첫 번째
	private String defaultFocus(LifeDTO life) {
		List<String> list = focusOptions(life.getStageCode(), life);
		if (list.contains("BALANCE")) {
			return "BALANCE";
		}
		return list.get(0);
	}

	// 집중별 1년 능력치 효과 (원작 수치: 건강, 행복, 지식, 관계)
	private void applyFocus(LifeDTO life) {
		String f = life.getFocusCode();
		if (f == null) {
			return; // 0~5세
		}
		double hl = 0, hp = 0, kn = 0, re = 0;
		switch (f) {
		case "STUDY":    kn = 5; hp = -1; re = -1;  break; // 공부
		case "PLAY":     re = 4; hp = 4;            break; // 친구·놀이
		case "HOUSE":    re = 2; kn = -1;           break; // 집안 돕기
		case "SPORT":    hl = 4; hp = 2; re = 1;    break; // 운동
		case "HOBBY":    hp = 4;                    break; // 취미
		case "PARTTIME": kn = -1; hp = -1; re = 1;  break; // 아르바이트 (소득은 C06)
		case "JOB_PREP": kn = 2; hp = -2;           break; // 취업 준비 (준비 점수는 C06)
		case "WORK":     hl = -3; hp = -2; re = -2; break; // 일에 몰두 (소득 배율은 C06)
		case "BALANCE":  hl = 1; hp = 1; re = 1;    break; // 균형
		case "FAMILY":   re = 5; hp = 3;            break; // 가족·친구
		case "HEALTH":   hl = 5; hp = 1;            break; // 건강 관리
		case "SELF_DEV": kn = 4; hp = -1;           break; // 자기계발
		case "BRAIN":    kn = 3; hp = 1; re = 1;    break; // 두뇌 활동
		case "REST":     hl = 2; hp = 2; kn = -1;   break; // 휴식
		}
		life.setHealth(life.getHealth() + hl);
		life.setHappiness(life.getHappiness() + hp);
		life.setKnowledge(life.getKnowledge() + kn);
		life.setRelation(life.getRelation() + re);
	}

	// ================= C05 능력치 =================

	// 나이별 기대 건강 (원작 기준): 0세 72, 1~4세 80, 5~29세 90, 30~59세 매년 -0.6, 60세부터 매년 -0.95 (최소 15)
	private double expHealth(int age) {
		if (age < 1) {
			return 72;
		} else if (age < 5) {
			return 80;
		} else if (age < 30) {
			return 90;
		} else if (age < 60) {
			return 90 - (age - 30) * 0.6;
		}
		return Math.max(15, 72 - (age - 60) * 0.95);
	}

	// 자연 변화 (원작 기준)
	private void naturalChange(LifeDTO life) {
		int age = life.getCurrentAge();
		double c = Math.min(1, Math.max(0, (age - 14) / 6.0)); // 15~20세에 걸쳐 어른 기준으로 바뀜

		// 1. 건강: 목표 = 기대 건강 + 가정형편 보정 + 건강 관리 집중, 매년 차이의 25%만큼 이동
		double healthGoal = expHealth(age + 1) + (life.getWealthClass() - 3) * 1.5;
		if ("HEALTH".equals(life.getFocusCode())) {
			healthGoal = healthGoal + 6;
		}
		life.setHealth(life.getHealth() + (healthGoal - life.getHealth()) * 0.25);

		// 2. 관계: 목표 = 45 + 결혼 12 + 자녀 수×4(최대 15) + 부모 생존 3 - 이민 6 (어른은 -8)
		double relGoal = 45;
		if ("Y".equals(life.getMarriedYn())) {
			relGoal = relGoal + 12;
		}
		relGoal = relGoal + Math.min(15, life.getChildCount() * 4);
		if ("Y".equals(life.getParentAlive())) {
			relGoal = relGoal + 3;
		}
		if (!life.getBirthCountry().equals(life.getCurrentCountry())) {
			relGoal = relGoal - 6;
		}
		relGoal = relGoal - 8 * c;
		double relRate = 0.12;
		if (life.getRelation() > relGoal) {
			relRate = 0.12 + (0.25 - 0.12) * c; // 어른은 높은 관계가 더 빨리 내려옴
		}
		life.setRelation(life.getRelation() + (relGoal - life.getRelation()) * relRate);

		// 3. 행복: 목표 = 기본 55.5 + 형편 보정 + 관계 보정 + 건강 보정 - 빚 (어른은 -12)
		double hapGoal = 55.5;
		if (age >= 18 && !"ENROLLED".equals(life.getSchoolStatus())) {
			hapGoal = hapGoal + ((life.getWealthClass() - 0.5) / 5 - 0.5) * 18; // 어른: 경제적 위치
		} else {
			hapGoal = hapGoal + (life.getWealthClass() - 3) * 3; // 아이: 가정형편
		}
		hapGoal = hapGoal + (life.getRelation() - 50) * 0.25 + (life.getHealth() - expHealth(age + 1)) * 0.2;
		if (life.getAssets() < 0) {
			hapGoal = hapGoal - 1;
		}
		hapGoal = hapGoal - 12 * c;
		double hapRate = 0.22 + ((life.getHappiness() > hapGoal ? 0.35 : 0.2) - 0.22) * c;
		life.setHappiness(life.getHappiness() + (hapGoal - life.getHappiness()) * hapRate);

		// 4. 지식 (원작 기준): 지식은 "또래 가운데 어디쯤인지"라서
		//    공부를 안 하면 또래 평균(50) 쪽으로 밀려남 (25세 전 매년 3%, 그 후 1.5%)
		if (age >= 6) {
			double keep = age < 25 ? 0.03 : 0.015;
			life.setKnowledge(life.getKnowledge() + (50 - life.getKnowledge()) * keep);

			// 학교 안 다니는 6~17세는 학교 다니는 또래보다 뒤처짐
			if (age <= 17 && !"ENROLLED".equals(life.getSchoolStatus())) {
				life.setKnowledge(life.getKnowledge() - 2);
			}
			// 73세부터는 두뇌 활동을 안 하면 조금씩 감소
			if (age > 72 && !"BRAIN".equals(life.getFocusCode())) {
				life.setKnowledge(life.getKnowledge() - 1);
			}
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
		if (idx == -1 || "GRADUATED".equals(life.getSchoolStatus())) {
			return idx; // 학교를 안 다녔으면 -1 (학력 없음)
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

	// ================= C09 이벤트 반영 =================

	// 올해 일어난 이벤트(윤아님 EventService.rollYear)를 인생에 반영
	// 선택 이벤트(한 해 최대 1개)는 바로 반영하지 않고 선택을 기다림 → chooseEvent
	private void eventStep(LifeDTO life) {
		List<EventResultDTO> events = eventService.rollYear(life);

		for (EventResultDTO e : events) {
			if (e.isChoice()) {
				// 상황 문장만 기록하고, 효과는 고른 선택지로 (PENDING_EVENT_ID에 저장)
				life.setPendingEventId(e.getEventId());
				life.setNeedChoice(true);
				life.setPendingEvent(e);
				saveEventLog(life, life.getCurrentAge(), e.getEventId(), null, e.getLogText());
				continue;
			}
			applyEvent(life, e, life.getCurrentAge(), null);
		}

		fixStats(life);
		life.setEvents(events); // [화면용] 올해 일어난 일
	}

	// 이벤트 결과 하나를 인생에 반영 (일반 이벤트, 고른 선택지 공통)
	// age: 이벤트가 일어난 나이, choiceId: 선택지 번호 (일반 이벤트는 null)
	private void applyEvent(LifeDTO life, EventResultDTO e, int age, Integer choiceId) {
		// 1. 능력치 변화 (C05)
		life.setHealth(life.getHealth() + e.getEffHealth());
		life.setHappiness(life.getHappiness() + e.getEffHappiness());
		life.setKnowledge(life.getKnowledge() + e.getEffKnowledge());
		life.setRelation(life.getRelation() + e.getEffRelation());

		// 2. 소득 배율 변화 (C06): 일하는 중일 때만, 0.1 = +10%
		if (e.getEffIncomeRate() != 0 && "EMPLOYED".equals(life.getJobStatus()) && life.getIncomeRate() != null) {
			life.setIncomeRate(life.getIncomeRate() * (1 + e.getEffIncomeRate()));
		}

		// 3. 재산 변화율 (C06 재산 반영): 재산 × 비율 (0.3 = +30%, -0.5 = -50%)
		//    재산이 0 이하(빚)면 변화 없음 → 빚이 있을 때 손실로 빚이 줄어드는 일 방지
		if (e.getEffWealthRate() != 0 && life.getAssets() > 0) {
			life.setAssets(life.getAssets() + Math.round(life.getAssets() * e.getEffWealthRate()));
		}

		// 4. 상태 변경 (결혼·자녀·부모 사망·실직·조기 은퇴·이민)
		applyAction(life, e.getResultAction(), e.getMigrateTo());

		// 5. 경험 기록: 윤아님 pickEvent의 '한 번만'·'필요 경험'·'제외 경험' 판정에 쓰임
		//    (선택지는 이벤트ID_순서, 예: LE_MIGRATE_WORK_1)
		String expId = e.getExpId() != null ? e.getExpId() : e.getEventId();
		ExperienceDTO exp = new ExperienceDTO();
		exp.setLifeId(life.getLifeId());
		exp.setExpId(expId);
		exp.setAge(age);
		exp.setYearNo(life.getBirthYear() + age);
		engineDao.insertExperience(exp);

		// 6. 인생 기록 문장 저장 (C03 기록 화면, C11 AI 서술 대상 표시)
		saveEventLog(life, age, e.getEventId(), choiceId, e.getLogText());
	}

	// LIFE_EVENT_LOG 한 줄 저장
	private void saveEventLog(LifeDTO life, int age, String eventId, Integer choiceId, String text) {
		EventLogDTO log = new EventLogDTO();
		log.setLifeId(life.getLifeId());
		log.setAge(age);
		log.setYearNo(life.getBirthYear() + age);
		log.setEventId(eventId);
		log.setChoiceId(choiceId);
		log.setLogText(text);
		engineDao.insertEventLog(log);
	}

	// ================= C09 선택 이벤트: 선택지 고르기 =================
	// 선택 이벤트는 작년(나이+1 하기 전)에 일어났으므로 그 나이로 기록
	public LifeDTO chooseEvent(int lifeId, int choiceId) {
		LifeDTO life = engineDao.selectLife(lifeId);
		if (life == null || !life.getStatus().equals("PLAYING") || life.getPendingEventId() == null) {
			return null; // 고를 선택이 없음
		}
		int age = life.getCurrentAge() - 1;

		EventResultDTO r = eventService.applyChoice(life, choiceId, age);
		if (r == null || !life.getPendingEventId().equals(r.getEventId())) {
			return null; // 없는 선택지이거나 지금 이벤트의 선택지가 아님
		}

		applyEvent(life, r, age, choiceId);
		fixStats(life);
		life.setPendingEventId(null);

		List<EventResultDTO> list = new ArrayList<>();
		list.add(r);
		life.setEvents(list); // [화면용] 고른 결과

		engineDao.updateLifeYear(life);
		return life;
	}

	// 선택을 기다리는 중인지 (다음 해로 못 넘어감)
	public boolean isWaitingChoice(int lifeId) {
		LifeDTO life = engineDao.selectLife(lifeId);
		return life != null && life.getPendingEventId() != null;
	}

	// 이어하기·새로고침 때 선택 창 다시 띄우기
	private void restoreChoice(LifeDTO life) {
		if (life == null || life.getPendingEventId() == null) {
			return;
		}
		EventResultDTO e = new EventResultDTO();
		e.setEventId(life.getPendingEventId());
		e.setChoice(true);
		e.setLogText(engineDao.selectPendingText(life));
		e.setChoices(eventDao.selectChoices(life.getPendingEventId())); // 윤아님 DAO (읽기만)
		life.setNeedChoice(true);
		life.setPendingEvent(e);
	}

	// 이벤트 결과의 상태 변경 처리 (RESULT_ACTION)
	private void applyAction(LifeDTO life, String action, String migrateTo) {
		if (action == null) {
			return;
		}
		switch (action) {
		case "MARRY": // 결혼
			life.setMarriedYn("Y");
			break;
		case "CHILD": // 자녀 +1
			life.setChildCount(life.getChildCount() + 1);
			break;
		case "PARENT_DEATH": // 부모 사망
			life.setParentAlive("N");
			break;
		case "JOB_LOSS": // 실직: 다음 해부터 다시 직업 제안 (jobStep)
			if ("EMPLOYED".equals(life.getJobStatus())) {
				life.setJobStatus("UNEMPLOYED");
				life.setJobYears(0);
			}
			break;
		case "EARLY_RETIRE": // 조기 은퇴: 정년 은퇴와 같이 연금 = 마지막 소득 × 0.3
			if ("EMPLOYED".equals(life.getJobStatus())) {
				life.setJobStatus("RETIRED");
				life.setPension(Math.round(life.getIncome() * 0.3));
			}
			break;
		case "MIGRATE": // 이민: 갈 나라가 정해져 있을 때만
			if (migrateTo != null && !migrateTo.equals(life.getCurrentCountry())) {
				life.setCurrentCountry(migrateTo);
			}
			break;
		default: // INVEST 등: 재산 변화율만 반영 (위 3번)
			break;
		}
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
		Double u5Mortality = null;
		String region = "";
		if (stat != null) {
			u5Mortality = stat.getU5Mortality();
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
			// 1~4세 (명세서): 5세 미만 사망률에서 영아사망을 뺀 나머지를 0.40·0.27·0.19·0.14 비율로 나눔
			//   1세를 넘긴 아이 기준 확률이라 (1 - 영아사망률)로 나눔
			//   5세 미만 사망률 데이터가 없으면 영아사망률의 30%로 추정
			double rest = infantMortality * 0.3;
			if (u5Mortality != null && u5Mortality > infantMortality) {
				rest = u5Mortality - infantMortality;
			}
			double[] share = { 0, 0.40, 0.27, 0.19, 0.14 };
			p = rest / 1000 * share[age] / (1 - infantMortality / 1000);
		} else {
			// 5세 이상: 나이가 들수록 빠르게 커지는 곡선
			// 기대수명이 낮은 나라·시대일수록 곡선이 앞당겨짐
			double shift = (75 - lifeExpectancy) * 0.75;
			p = 0.000065 * Math.exp(0.09 * (age + shift));
		}

		// 2. 성별: 남자 ×1.08, 여자 ×0.92
		if (life.getGender().equals("M")) {
			p = p * 1.08;
		} else {
			p = p * 0.92;
		}

		if (p > 1) {
			p = 1;
		}

		// 3. 그해 사망 배율 (원작 기준)
		// 건강: e^((기대 건강 - 6 - 50세 이후 (나이-50)×0.08 - 내 건강) × 0.03), 5세 미만 0.02, 최소 0.5
		double baseHealth = expHealth(age) - 6;
		if (age > 50) {
			baseHealth = baseHealth - (age - 50) * 0.08;
		}
		double k = 0.03;
		if (age < 5) {
			k = 0.02;
		}
		double healthRate = Math.exp((baseHealth - life.getHealth()) * k);
		if (healthRate < 0.5) {
			healthRate = 0.5;
		}

		// 가정형편: e^(-r × (분위 - 3)) ÷ 1~5분위 평균 (남아시아 0.25, 서아시아 0.15, 그 외 0.2)
		double r = 0.2;
		if (region.equals("SOUTH")) {
			r = 0.25;
		} else if (region.equals("WEST")) {
			r = 0.15;
		}
		double avg = 0;
		for (int d = 1; d <= 5; d++) {
			avg = avg + Math.exp(-r * (d - 3));
		}
		avg = avg / 5;
		double wealthRate = Math.exp(-r * (life.getWealthClass() - 3)) / avg;

		double rate = healthRate * wealthRate;
		if (rate < 0.25) {
			rate = 0.25;
		}
		if (rate > 9) {
			rate = 9;
		}

		// 85세 이상은 배율 효과를 점점 줄임
		if (age >= 85) {
			double weaken = 1 - (age - 85) / 20.0;
			if (weaken < 0.3) {
				weaken = 0.3;
			}
			rate = Math.pow(rate, weaken);
		}

		// 배율 적용: 1 - (1 - 기본 확률)^배율 (확률이 1을 넘지 않게)
		p = 1 - Math.pow(1 - p, rate);

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

		// 소득 상위 % = 100 - (내 평균 소득보다 1인당 GDP가 낮은 나라에 사는 인구 비율)
		Double poorer = engineDao.selectPoorerPct(life);
		if (poorer != null) {
			double top = 100 - poorer;
			if (top < 0.1) {
				top = 0.1; // 상위 0.1% 미만은 0.1%로 표시
			}
			life.setIncomeTopPct(Math.round(top * 10) / 10.0);
		}
	}
}
