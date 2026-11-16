package com.example.demo.engine;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EngineDao {
	String selectNow();

	// C02 출생
	List<CountryStatDTO> selectBirthCountries(int decade);
	String selectRandomName(LifeDTO life);
	String selectHistName(LifeDTO life);
	int selectNextLifeId();
	int abandonGuestLife(String guestToken);
	int abandonMemberLife(int memberId);
	int moveOldGuestStats();
	int deleteOldGuestLives();
	int insertLife(LifeDTO life);

	// C03 1년 진행
	LifeDTO selectLife(int lifeId);
	LifeDTO selectPlayingLife(LifeDTO cond);
	int updateLifeYear(LifeDTO life);
	int insertYearLog(LifeDTO life);

	// C07 사망 판정
	CountryStatDTO selectYearStat(LifeDTO life);
	int endLife(LifeDTO life);
	List<DeathCauseDTO> selectDeathCauses(DeathCauseDTO cond);
	Long selectAvgIncome(int lifeId);
	Double selectPoorerPct(LifeDTO life);

	// C04 집중 선택
	int updateFocus(LifeDTO life);

	// C05 경험
	int insertExperience(ExperienceDTO exp);

	// C06 진학
	RegionStatDTO selectRegionStat(LifeDTO life);

	// C06 직업
	List<JobDTO> selectJobs();
	JobDTO selectJob(String jobId);
}
