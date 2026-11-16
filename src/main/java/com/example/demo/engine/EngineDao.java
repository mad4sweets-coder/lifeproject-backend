package com.example.demo.engine;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EngineDao {
	String selectNow();

	List<CountryStatDTO> selectBirthCountries(int decade);
	String selectRandomName(LifeDTO life);
	String selectHistName(LifeDTO life);
	int selectNextLifeId();
	int abandonGuestLife(String guestToken);
	int insertLife(LifeDTO life);
}
