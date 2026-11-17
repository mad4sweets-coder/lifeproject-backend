package com.example.demo.admin;

// A04 퀘스트별 달성률
public class QuestRateDTO {
	private int questId;	// 퀘스트 번호
	private String questName;	// 퀘스트명
	private int picked;	// 고른 인생 수 (판정 끝난 것)
	private int achieved;	// 달성한 인생 수
	private double rate;	// 달성률 %

	public int getQuestId() {
		return questId;
	}

	public void setQuestId(int questId) {
		this.questId = questId;
	}

	public String getQuestName() {
		return questName;
	}

	public void setQuestName(String questName) {
		this.questName = questName;
	}

	public int getPicked() {
		return picked;
	}

	public void setPicked(int picked) {
		this.picked = picked;
	}

	public int getAchieved() {
		return achieved;
	}

	public void setAchieved(int achieved) {
		this.achieved = achieved;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}
}
