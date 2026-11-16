package com.example.demo.engine;

public class CountryStatDTO {
	private String countryCode;	// 나라 코드
	private String nameKo;	// 한글 이름
	private String regionCode;	// 권역 (EAST, SOUTHEAST, SOUTH, CENTRAL, WEST)
	private Long births;	// 출생아 수
	private Double lifeExpectancy;	// 기대수명
	private Double infantMortality;	// 영아사망률(‰)
	private Double u5Mortality;	// 5세 미만 사망률(‰)
	private Double gdpPerCapita;	// 1인당 GDP
	private Double urbanRate;	// 도시화율(%)
	private Double sexRatioBirth;	// 출생 성비(여아 1당 남아)

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getNameKo() {
		return nameKo;
	}

	public void setNameKo(String nameKo) {
		this.nameKo = nameKo;
	}

	public String getRegionCode() {
		return regionCode;
	}

	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}

	public Long getBirths() {
		return births;
	}

	public void setBirths(Long births) {
		this.births = births;
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

	public Double getUrbanRate() {
		return urbanRate;
	}

	public void setUrbanRate(Double urbanRate) {
		this.urbanRate = urbanRate;
	}

	public Double getSexRatioBirth() {
		return sexRatioBirth;
	}

	public void setSexRatioBirth(Double sexRatioBirth) {
		this.sexRatioBirth = sexRatioBirth;
	}

	public Double getU5Mortality() {
		return u5Mortality;
	}

	public void setU5Mortality(Double u5Mortality) {
		this.u5Mortality = u5Mortality;
	}

}
