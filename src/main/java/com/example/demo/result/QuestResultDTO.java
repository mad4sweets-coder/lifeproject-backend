package com.example.demo.result;

// C18 퀘스트 달성 결과 1줄
public class QuestResultDTO {
	private int questId;	// 퀘스트 번호
	private String questName;	// 퀘스트명
	private String description;	// 설명
	private String achieved;	// 달성 여부 Y, N (판정 전 null)
	private String pointGiven;	// 포인트 지급 여부 Y, N

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAchieved() {
		return achieved;
	}

	public void setAchieved(String achieved) {
		this.achieved = achieved;
	}

	public String getPointGiven() {
		return pointGiven;
	}

	public void setPointGiven(String pointGiven) {
		this.pointGiven = pointGiven;
	}
}
