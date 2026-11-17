package com.example.demo.qa;

// C20 질문 보내기 요청 { "question": "...", "guestToken": "..." }
public class QaRequestDTO {
	private String question;	// 질문
	private String guestToken;	// 비회원 임시 토큰 (회원은 null)

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getGuestToken() {
		return guestToken;
	}

	public void setGuestToken(String guestToken) {
		this.guestToken = guestToken;
	}
}
