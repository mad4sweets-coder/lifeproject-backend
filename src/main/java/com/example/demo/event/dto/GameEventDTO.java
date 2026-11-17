package com.example.demo.event.dto;

public class GameEventDTO {
	private String eventId;
	private String eventType;
	private String templateId;
	private String varValue;
	private Double effectScale;
	private String histEventId;
	private String histTypeCode;
	private Integer intensity;
	private String title;
	private String baseText;
	private String isReviewed;
	private Integer minAge;
	private Integer maxAge;
	private String gender;
	private Integer minWealth;
	private Integer maxWealth;
	private String requiredExpId;
	private String excludedExpId;
	private String isOnce;
	private Double probability;
	private String isChoice;
	private String aiNarrate;
	private Double effWealthRate;

	public GameEventDTO() {}
	
	public GameEventDTO(String eventId, String eventType, String templateId, String varValue, Double effectScale,
			String histEventId, String histTypeCode, Integer intensity, String title, String baseText,
			String isReviewed, Integer minAge, Integer maxAge, String gender, Integer minWealth, Integer maxWealth,
			String requiredExpId, String excludedExpId, String isOnce, Double probability, String isChoice,
			String aiNarrate) {
		this.eventId = eventId;
		this.eventType = eventType;
		this.templateId = templateId;
		this.varValue = varValue;
		this.effectScale = effectScale;
		this.histEventId = histEventId;
		this.histTypeCode = histTypeCode;
		this.intensity = intensity;
		this.title = title;
		this.baseText = baseText;
		this.isReviewed = isReviewed;
		this.minAge = minAge;
		this.maxAge = maxAge;
		this.gender = gender;
		this.minWealth = minWealth;
		this.maxWealth = maxWealth;
		this.requiredExpId = requiredExpId;
		this.excludedExpId = excludedExpId;
		this.isOnce = isOnce;
		this.probability = probability;
		this.isChoice = isChoice;
		this.aiNarrate = aiNarrate;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getVarValue() {
		return varValue;
	}

	public void setVarValue(String varValue) {
		this.varValue = varValue;
	}

	public Double getEffectScale() {
		return effectScale;
	}

	public void setEffectScale(Double effectScale) {
		this.effectScale = effectScale;
	}

	public String getHistEventId() {
		return histEventId;
	}

	public void setHistEventId(String histEventId) {
		this.histEventId = histEventId;
	}

	public String getHistTypeCode() {
		return histTypeCode;
	}

	public void setHistTypeCode(String histTypeCode) {
		this.histTypeCode = histTypeCode;
	}

	public Integer getIntensity() {
		return intensity;
	}

	public void setIntensity(Integer intensity) {
		this.intensity = intensity;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBaseText() {
		return baseText;
	}

	public void setBaseText(String baseText) {
		this.baseText = baseText;
	}

	public String getIsReviewed() {
		return isReviewed;
	}

	public void setIsReviewed(String isReviewed) {
		this.isReviewed = isReviewed;
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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Integer getMinWealth() {
		return minWealth;
	}

	public void setMinWealth(Integer minWealth) {
		this.minWealth = minWealth;
	}

	public Integer getMaxWealth() {
		return maxWealth;
	}

	public void setMaxWealth(Integer maxWealth) {
		this.maxWealth = maxWealth;
	}

	public String getRequiredExpId() {
		return requiredExpId;
	}

	public void setRequiredExpId(String requiredExpId) {
		this.requiredExpId = requiredExpId;
	}

	public String getExcludedExpId() {
		return excludedExpId;
	}

	public void setExcludedExpId(String excludedExpId) {
		this.excludedExpId = excludedExpId;
	}

	public String getIsOnce() {
		return isOnce;
	}

	public void setIsOnce(String isOnce) {
		this.isOnce = isOnce;
	}

	public Double getProbability() {
		return probability;
	}

	public void setProbability(Double probability) {
		this.probability = probability;
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
	
	public Double getEffWealthRate() {
		return effWealthRate;
	}

	public void setEffWealthRate(Double effWealthRate) {
		this.effWealthRate = effWealthRate;
	}
}