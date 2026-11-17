package com.example.demo.event.dto;

public class DestinationDTO {

	private String countryCode; // 나라 코드
	private String nameKo; // 나라 이름
	private Double gdpPerCapita; // 그 시대 1인당 GDP
	private int tier; // 구역 1(상위) ~ 5(하위)
	private String tierName; // 상위, 중상위, 중위, 중하위, 하위

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

	public Double getGdpPerCapita() {
		return gdpPerCapita;
	}

	public void setGdpPerCapita(Double gdpPerCapita) {
		this.gdpPerCapita = gdpPerCapita;
	}

	public int getTier() {
		return tier;
	}

	public void setTier(int tier) {
		this.tier = tier;
	}

	public String getTierName() {
		return tierName;
	}

	public void setTierName(String tierName) {
		this.tierName = tierName;
	}
}