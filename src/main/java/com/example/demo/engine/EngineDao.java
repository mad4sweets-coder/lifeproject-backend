package com.example.demo.engine;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EngineDao {
	String selectNow();

	List<CountryStatVO> selectBirthCountries(int decade);
	String selectRandomName(LifeVO life);
	String selectHistName(LifeVO life);
	int selectNextLifeId();
	int abandonGuestLife(String guestToken);
	int insertLife(LifeVO life);
}
