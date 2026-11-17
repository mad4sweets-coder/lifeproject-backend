package com.example.demo.qa;

import java.util.List;

import com.example.demo.story.SourceDTO;

// C20 질문·답변 1개 (QA_LOG)
public class QaDTO {
	private int qaId;					// 번호
	private int lifeId;					// 인생 번호
	private String question;			// 질문
	private String answer;				// 답변 (문장 끝 [1] = sources 의 no)
	private String sourceIds;			// 근거로 넘긴 출처 번호 목록 "3,7,12" (저장용)
	private String createdAt;			// 일시
	private List<SourceDTO> sources;	// 답변에서 실제로 인용한 출처만

	public int getQaId() {
		return qaId;
	}

	public void setQaId(int qaId) {
		this.qaId = qaId;
	}

	public int getLifeId() {
		return lifeId;
	}

	public void setLifeId(int lifeId) {
		this.lifeId = lifeId;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getSourceIds() {
		return sourceIds;
	}

	public void setSourceIds(String sourceIds) {
		this.sourceIds = sourceIds;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public List<SourceDTO> getSources() {
		return sources;
	}

	public void setSources(List<SourceDTO> sources) {
		this.sources = sources;
	}
}
