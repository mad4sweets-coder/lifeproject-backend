package com.example.demo.story;

// 근거 자료: 살았던 연대의 나라 통계 (COUNTRY_STAT + 출처)
public class DecadeStatDTO {
	private int decade;	// 연대
	private Double lifeExpectancy;	// 기대수명
	private Double gdpPerCapita;	// 1인당 GDP (달러)
	private Double urbanRate;	// 도시화율 %
	private Double tfr;	// 합계출산율
	private String isEstimated;	// 추정 여부 Y, N
	private Integer sourceId;	// 출처 번호
	private String sourceTitle;	// 자료명
	private String sourceUrl;	// URL

	public int getDecade() {
		return decade;
	}

	public void setDecade(int decade) {
		this.decade = decade;
	}

	public Double getLifeExpectancy() {
		return lifeExpectancy;
	}

	public void setLifeExpectancy(Double lifeExpectancy) {
		this.lifeExpectancy = lifeExpectancy;
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

	public Double getTfr() {
		return tfr;
	}

	public void setTfr(Double tfr) {
		this.tfr = tfr;
	}

	public String getIsEstimated() {
		return isEstimated;
	}

	public void setIsEstimated(String isEstimated) {
		this.isEstimated = isEstimated;
	}

	public Integer getSourceId() {
		return sourceId;
	}

	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}

	public String getSourceTitle() {
		return sourceTitle;
	}

	public void setSourceTitle(String sourceTitle) {
		this.sourceTitle = sourceTitle;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
}
