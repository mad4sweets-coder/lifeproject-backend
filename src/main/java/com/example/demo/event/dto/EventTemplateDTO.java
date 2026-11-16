package com.example.demo.event.dto;

public class EventTemplateDTO {
	private String templateId;
	private String templateName;
	private Integer minAge;
	private Integer maxAge;
	private String probMethod; // TFR, UNEMPLOYMENT, NET_MIGRATION, DEATH_CAUSE, FIXED
	private Double baseProb;
	private String isChoice; // Y/N
	private String aiNarrate; // Y/N
	private Integer effHealth;
	private Integer effHappiness;
	private Integer effKnowledge;
	private Integer effRelation;
	private Double effIncomeRate;
	private Double effWealthRate;
	private String resultAction; // MARRY, CHILD, PARENT_DEATH, JOB_LOSS, EARLY_RETIRE, MIGRATE, INVEST
	private String traitId;

	public EventTemplateDTO() {}
	
	public EventTemplateDTO(String templateId, String templateName, Integer minAge, Integer maxAge, String probMethod,
			Double baseProb, String isChoice, String aiNarrate, Integer effHealth, Integer effHappiness,
			Integer effKnowledge, Integer effRelation, Double effIncomeRate, Double effWealthRate, String resultAction,
			String traitId) {
		this.templateId = templateId;
		this.templateName = templateName;
		this.minAge = minAge;
		this.maxAge = maxAge;
		this.probMethod = probMethod;
		this.baseProb = baseProb;
		this.isChoice = isChoice;
		this.aiNarrate = aiNarrate;
		this.effHealth = effHealth;
		this.effHappiness = effHappiness;
		this.effKnowledge = effKnowledge;
		this.effRelation = effRelation;
		this.effIncomeRate = effIncomeRate;
		this.effWealthRate = effWealthRate;
		this.resultAction = resultAction;
		this.traitId = traitId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public Integer getMinAge() {
		return minAge;
	}

	public void setMinAge(Integer minAge) {
		this.minAge = minAge;
	}

	public Integer getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(Integer maxAge) {
		this.maxAge = maxAge;
	}

	public String getProbMethod() {
		return probMethod;
	}

	public void setProbMethod(String probMethod) {
		this.probMethod = probMethod;
	}

	public Double getBaseProb() {
		return baseProb;
	}

	public void setBaseProb(Double baseProb) {
		this.baseProb = baseProb;
	}

	public String getIsChoice() {
		return isChoice;
	}

	public void setIsChoice(String isChoice) {
		this.isChoice = isChoice;
	}

	public String getAiNarrate() {
		return aiNarrate;
	}

	public void setAiNarrate(String aiNarrate) {
		this.aiNarrate = aiNarrate;
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

	public Double getEffIncomeRate() {
		return effIncomeRate;
	}

	public void setEffIncomeRate(Double effIncomeRate) {
		this.effIncomeRate = effIncomeRate;
	}

	public Double getEffWealthRate() {
		return effWealthRate;
	}

	public void setEffWealthRate(Double effWealthRate) {
		this.effWealthRate = effWealthRate;
	}

	public String getResultAction() {
		return resultAction;
	}

	public void setResultAction(String resultAction) {
		this.resultAction = resultAction;
	}

	public String getTraitId() {
		return traitId;
	}

	public void setTraitId(String traitId) {
		this.traitId = traitId;
	}
}