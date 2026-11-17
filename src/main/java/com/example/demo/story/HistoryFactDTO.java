package com.example.demo.story;

// 근거 자료: 살았던 나라·기간의 역사 사건 (HISTORY_EVENT + 출처)
public class HistoryFactDTO {
	private String eventId;	// 사건 ID
	private String title;	// 사건명
	private int startYear;	// 시작 연도
	private int endYear;	// 끝 연도
	private String description;	// 사실 설명
	private String experienced;	// 이 인생에서 실제로 겪은 사건이면 Y
	private Integer sourceId;	// 출처 번호
	private String sourceTitle;	// 자료명
	private String sourceUrl;	// URL

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getStartYear() {
		return startYear;
	}

	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}

	public int getEndYear() {
		return endYear;
	}

	public void setEndYear(int endYear) {
		this.endYear = endYear;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExperienced() {
		return experienced;
	}

	public void setExperienced(String experienced) {
		this.experienced = experienced;
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
