package com.example.demo.admin;

// A04 특성별 레벨 분포 (회원 기준)
public class TraitLevelDTO {
	private String traitId;	// 특성 코드
	private String traitName;	// 특성명
	private double avgLevel;	// 평균 레벨
	private int lv1;	// 1레벨 회원 수
	private int lv2;	// 2레벨
	private int lv3;	// 3레벨
	private int lv4;	// 4레벨
	private int lv5;	// 5레벨

	public String getTraitId() {
		return traitId;
	}

	public void setTraitId(String traitId) {
		this.traitId = traitId;
	}

	public String getTraitName() {
		return traitName;
	}

	public void setTraitName(String traitName) {
		this.traitName = traitName;
	}

	public double getAvgLevel() {
		return avgLevel;
	}

	public void setAvgLevel(double avgLevel) {
		this.avgLevel = avgLevel;
	}

	public int getLv1() {
		return lv1;
	}

	public void setLv1(int lv1) {
		this.lv1 = lv1;
	}

	public int getLv2() {
		return lv2;
	}

	public void setLv2(int lv2) {
		this.lv2 = lv2;
	}

	public int getLv3() {
		return lv3;
	}

	public void setLv3(int lv3) {
		this.lv3 = lv3;
	}

	public int getLv4() {
		return lv4;
	}

	public void setLv4(int lv4) {
		this.lv4 = lv4;
	}

	public int getLv5() {
		return lv5;
	}

	public void setLv5(int lv5) {
		this.lv5 = lv5;
	}
}
