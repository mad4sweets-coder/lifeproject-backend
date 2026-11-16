package com.example.demo.engine;

public class DeathCauseDTO {
	private String countryCode;	// [조회 조건] 사는 나라
	private String ageGroup;	// [조회 조건] 나이대 0-4, 5-14, 15-49, 50-69, 70+
	private int yearNo;	// [조회 조건] 그해 연도
	private String causeName;	// 사망 원인
	private double ratio;	// 비율 (%)

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getAgeGroup() {
		return ageGroup;
	}

	public void setAgeGroup(String ageGroup) {
		this.ageGroup = ageGroup;
	}

	public int getYearNo() {
		return yearNo;
	}

	public void setYearNo(int yearNo) {
		this.yearNo = yearNo;
	}

	public String getCauseName() {
		return causeName;
	}

	public void setCauseName(String causeName) {
		this.causeName = causeName;
	}

	public double getRatio() {
		return ratio;
	}

	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

}
