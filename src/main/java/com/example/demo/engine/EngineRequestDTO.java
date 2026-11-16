package com.example.demo.engine;

// React에서 POST로 보내는 요청 JSON을 담는 그릇
// 예: { "decade": 1950, "guestToken": "..." } / { "lifeId": 3, "focus": "SPORT", "guestToken": "..." }
public class EngineRequestDTO {
	private int decade;        // 출생 연대 (C02)
	private int lifeId;        // 인생 번호 (C03, C04, C06)
	private String guestToken; // 비회원 임시 토큰 (회원은 Authorization 헤더 사용)
	private String focus;      // 집중 코드 (C04)
	private String jobId;      // 직업 코드 (C06)

	public int getDecade() {
		return decade;
	}

	public void setDecade(int decade) {
		this.decade = decade;
	}

	public int getLifeId() {
		return lifeId;
	}

	public void setLifeId(int lifeId) {
		this.lifeId = lifeId;
	}

	public String getGuestToken() {
		return guestToken;
	}

	public void setGuestToken(String guestToken) {
		this.guestToken = guestToken;
	}

	public String getFocus() {
		return focus;
	}

	public void setFocus(String focus) {
		this.focus = focus;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
}
