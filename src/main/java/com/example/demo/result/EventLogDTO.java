package com.example.demo.result;

// C18 인생 기록 1줄 (그해 일어난 일)
public class EventLogDTO {
	private int logId;	// 기록 번호
	private int age;	// 나이
	private int yearNo;	// 연도
	private String eventId;	// 이벤트 ID (진학·취업 등은 null)
	private String choiceText;	// 고른 선택지 문구 (선택 이벤트일 때)
	private String logText;	// 표시 문구
	private String isAi;	// AI 서술 여부 Y, N

	public int getLogId() {
		return logId;
	}

	public void setLogId(int logId) {
		this.logId = logId;
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

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getChoiceText() {
		return choiceText;
	}

	public void setChoiceText(String choiceText) {
		this.choiceText = choiceText;
	}

	public String getLogText() {
		return logText;
	}

	public void setLogText(String logText) {
		this.logText = logText;
	}

	public String getIsAi() {
		return isAi;
	}

	public void setIsAi(String isAi) {
		this.isAi = isAi;
	}
}
