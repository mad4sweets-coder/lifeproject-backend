package com.example.demo.admin;

// A04 나라별 게임 평균 수명 vs 실제 기대수명 (시뮬레이션 검증)
public class CountryLifeDTO {
	private String countryCode;	// 나라 코드
	private String countryName;	// 나라 이름
	private int lives;	// 끝까지 산 인생 수
	private double avgDeathAge;	// 게임 평균 수명
	private Double realLifeExpectancy;	// 실제 기대수명 (그 인생들이 태어난 연대 값의 평균, C16)

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public int getLives() {
		return lives;
	}

	public void setLives(int lives) {
		this.lives = lives;
	}

	public double getAvgDeathAge() {
		return avgDeathAge;
	}

	public void setAvgDeathAge(double avgDeathAge) {
		this.avgDeathAge = avgDeathAge;
	}

	public Double getRealLifeExpectancy() {
		return realLifeExpectancy;
	}

	public void setRealLifeExpectancy(Double realLifeExpectancy) {
		this.realLifeExpectancy = realLifeExpectancy;
	}
}
