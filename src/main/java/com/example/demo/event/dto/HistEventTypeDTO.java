package com.example.demo.event.dto;

public class HistEventTypeDTO {

	private String typeCode; // WAR, ECON, DISASTER, POLITICS, REFUGE
	private String typeName;
	private Integer effHealth; // 강도 1 기준
	private Integer effHappiness;
	private Integer effKnowledge;
	private Integer effRelation;
	private Double jobLossAdd; // 실직 확률 보정 (엔진용)
	private Double migrateAdd; // 이민 확률 보정 (엔진용)
	private Double eduStopAdd; // 학업 중단 확률 보정 (엔진용)
	private Double deathAdd; // 사망 확률 가산값
	private String deathCause; // 사망 원인명
	private Double effWealthRate; // 재산 변화율 (강도 1 기준)

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Integer getEffHealth() {
		return effHealth;
	}

	public void setEffHealth(Integer effHealth) {
		this.effHealth = effHealth;
	}

	public Integer getEffHappiness() {
		return effHappiness;
	}

	public void setEffHappiness(Integer effHappiness) {
		this.effHappiness = effHappiness;
	}

	public Integer getEffKnowledge() {
		return effKnowledge;
	}

	public void setEffKnowledge(Integer effKnowledge) {
		this.effKnowledge = effKnowledge;
	}

	public Integer getEffRelation() {
		return effRelation;
	}

	public void setEffRelation(Integer effRelation) {
		this.effRelation = effRelation;
	}

	public Double getJobLossAdd() {
		return jobLossAdd;
	}

	public void setJobLossAdd(Double jobLossAdd) {
		this.jobLossAdd = jobLossAdd;
	}

	public Double getMigrateAdd() {
		return migrateAdd;
	}

	public void setMigrateAdd(Double migrateAdd) {
		this.migrateAdd = migrateAdd;
	}

	public Double getEduStopAdd() {
		return eduStopAdd;
	}

	public void setEduStopAdd(Double eduStopAdd) {
		this.eduStopAdd = eduStopAdd;
	}

	public Double getDeathAdd() {
		return deathAdd;
	}

	public void setDeathAdd(Double deathAdd) {
		this.deathAdd = deathAdd;
	}

	public String getDeathCause() {
		return deathCause;
	}

	public void setDeathCause(String deathCause) {
		this.deathCause = deathCause;
	}

	public Double getEffWealthRate() {
		return effWealthRate;
	}

	public void setEffWealthRate(Double effWealthRate) {
		this.effWealthRate = effWealthRate;
	}
}