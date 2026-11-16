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
	int insertLife(LifeDTO life);

	// C03 1년 진행
	LifeDTO selectLife(int lifeId);
	int updateLifeYear(LifeDTO life);
	int insertYearLog(LifeDTO life);

	// C07 사망 판정
	CountryStatDTO selectYearStat(LifeDTO life);
	int endLife(LifeDTO life);
}
