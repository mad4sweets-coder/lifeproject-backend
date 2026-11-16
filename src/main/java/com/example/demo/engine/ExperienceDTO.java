package com.example.demo.engine;

public class ExperienceDTO {
	private int lifeId;	// 인생 번호
	private String expId;	// 경험 ID (예: EDU_UNI, JOB_DOCTOR)
	private int age;	// 나이
	private int yearNo;	// 연도

	public int getLifeId() {
		return lifeId;
	}

	public void setLifeId(int lifeId) {
		this.lifeId = lifeId;
	}

	public String getExpId() {
		return expId;
	}

	public void setExpId(String expId) {
		this.expId = expId;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getYearNo() {
		return yearNo;
	}

	public void setYearNo(int yearNo) {
		this.yearNo = yearNo;
	}

}
