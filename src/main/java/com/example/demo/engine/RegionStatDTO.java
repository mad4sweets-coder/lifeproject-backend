package com.example.demo.engine;

public class RegionStatDTO {
	private Double enrollElem;	// 초등 진학률 (%)
	private Double enrollMiddle;	// 중등 진학률 (%)
	private Double enrollHigh;	// 고등 진학률 (%)
	private Double enrollUni;	// 대학 진학률 (%)
	private Double agriRate;	// 농업 취업자 비율 (%)
	private Double industryRate;	// 공업 취업자 비율 (%)
	private Double serviceRate;	// 서비스 취업자 비율 (%)

	public Double getEnrollElem() {
		return enrollElem;
	}

	public void setEnrollElem(Double enrollElem) {
		this.enrollElem = enrollElem;
	}

	public Double getEnrollMiddle() {
		return enrollMiddle;
	}

	public void setEnrollMiddle(Double enrollMiddle) {
		this.enrollMiddle = enrollMiddle;
	}

	public Double getEnrollHigh() {
		return enrollHigh;
	}

	public void setEnrollHigh(Double enrollHigh) {
		this.enrollHigh = enrollHigh;
	}

	public Double getEnrollUni() {
		return enrollUni;
	}

	public void setEnrollUni(Double enrollUni) {
		this.enrollUni = enrollUni;
	}

	public Double getAgriRate() {
		return agriRate;
	}

	public void setAgriRate(Double agriRate) {
		this.agriRate = agriRate;
	}

	public Double getIndustryRate() {
		return industryRate;
	}

	public void setIndustryRate(Double industryRate) {
		this.industryRate = industryRate;
	}

	public Double getServiceRate() {
		return serviceRate;
	}

	public void setServiceRate(Double serviceRate) {
		this.serviceRate = serviceRate;
	}

}
