package com.example.demo.engine;

import java.util.List;

public class LifeDTO {
	private int lifeId;	// 인생 번호
	private Integer memberId;	// 회원 번호 (비회원 null)
	private String guestToken;	// 비회원 임시 토큰
	private String startType;	// MEMBER, GUEST
	private String name;	// 캐릭터 이름
	private String gender;	// M, F
	private String birthCountry;	// 태어난 나라
	private String currentCountry;	// 현재 사는 나라
	private int birthYear;	// 출생 연도
	private int wealthClass;	// 가정형편 1~5
	private String isUrban;	// 도시 여부 Y, N
	private int currentAge;	// 현재 나이
	private String status;	// PLAYING, ENDED, ABANDONED
	private double health;	// 건강
	private double happiness;	// 행복
	private double knowledge;	// 지식
	private double relation;	// 관계
	private String stageCode;	// 나이대 (C04)
	private String focusCode;	// 집중 선택 (C04)
	private Integer deathAge;	// 사망 나이 (C07)
	private String deathCause;	// 사망 원인 (C07)
	private boolean needFocus;	// [화면용] 집중 선택 창 띄우기 (C04)
	private List<String> focusOptions;	// [화면용] 고를 수 있는 집중 목록 (C04)
	private String countryName;	// [화면용] 나라 이름
	private String histName;	// [화면용] 당시 국가명
	private Double lifeExpectancy;	// [화면용] 기대수명
	private Double infantMortality;	// [화면용] 영아사망률
	private Double gdpPerCapita;	// [화면용] 1인당 GDP

	public int getLifeId() {
		return lifeId;
	}

	public void setLifeId(int lifeId) {
		this.lifeId = lifeId;
	}

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

	public String getGuestToken() {
		return guestToken;
	}

	public void setGuestToken(String guestToken) {
		this.guestToken = guestToken;
	}

	public String getStartType() {
		return startType;
	}

	public void setStartType(String startType) {
		this.startType = startType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getBirthCountry() {
		return birthCountry;
	}

	public void setBirthCountry(String birthCountry) {
		this.birthCountry = birthCountry;
	}

	public String getCurrentCountry() {
		return currentCountry;
	}

	public void setCurrentCountry(String currentCountry) {
		this.currentCountry = currentCountry;
	}

	public int getBirthYear() {
		return birthYear;
	}

	public void setBirthYear(int birthYear) {
		this.birthYear = birthYear;
	}

	public int getWealthClass() {
		return wealthClass;
	}

	public void setWealthClass(int wealthClass) {
		this.wealthClass = wealthClass;
	}

	public String getIsUrban() {
		return isUrban;
	}

	public void setIsUrban(String isUrban) {
		this.isUrban = isUrban;
	}

	public int getCurrentAge() {
		return currentAge;
	}

	public void setCurrentAge(int currentAge) {
		this.currentAge = currentAge;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public double getHealth() {
		return health;
	}

	public void setHealth(double health) {
		this.health = health;
	}

	public double getHappiness() {
		return happiness;
	}

	public void setHappiness(double happiness) {
		this.happiness = happiness;
	}

	public double getKnowledge() {
		return knowledge;
	}

	public void setKnowledge(double knowledge) {
		this.knowledge = knowledge;
	}

	public double getRelation() {
		return relation;
	}

	public void setRelation(double relation) {
		this.relation = relation;
	}

	public String getStageCode() {
		return stageCode;
	}

	public void setStageCode(String stageCode) {
		this.stageCode = stageCode;
	}

	public String getFocusCode() {
		return focusCode;
	}

	public void setFocusCode(String focusCode) {
		this.focusCode = focusCode;
	}

	public Integer getDeathAge() {
		return deathAge;
	}

	public void setDeathAge(Integer deathAge) {
		this.deathAge = deathAge;
	}

	public String getDeathCause() {
		return deathCause;
	}

	public void setDeathCause(String deathCause) {
		this.deathCause = deathCause;
	}

	public boolean isNeedFocus() {
		return needFocus;
	}

	public void setNeedFocus(boolean needFocus) {
		this.needFocus = needFocus;
	}

	public List<String> getFocusOptions() {
		return focusOptions;
	}

	public void setFocusOptions(List<String> focusOptions) {
		this.focusOptions = focusOptions;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getHistName() {
		return histName;
	}

	public void setHistName(String histName) {
		this.histName = histName;
	}

	public Double getLifeExpectancy() {
		return lifeExpectancy;
	}

	public void setLifeExpectancy(Double lifeExpectancy) {
		this.lifeExpectancy = lifeExpectancy;
	}

	public Double getInfantMortality() {
		return infantMortality;
	}

	public void setInfantMortality(Double infantMortality) {
		this.infantMortality = infantMortality;
	}

	public Double getGdpPerCapita() {
		return gdpPerCapita;
	}

	public void setGdpPerCapita(Double gdpPerCapita) {
		this.gdpPerCapita = gdpPerCapita;
	}

}
