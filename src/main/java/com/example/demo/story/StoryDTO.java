package com.example.demo.story;

import java.util.List;

// C19 AI 인생 이야기 (화면에 보낼 값 + AI_STORY 저장용)
public class StoryDTO {
	private int lifeId;	// 인생 번호
	private String story;	// 이야기 본문 (문장 끝 [1] = sources 1번째)
	private String sourceIds;	// 근거 출처 번호 목록 "3,7,12" (AI_STORY 저장용, 화면에는 sources 사용)
	private String createdAt;	// 만든 날짜
	private List<SourceDTO> sources;	// 근거 출처 목록 (이야기 속 [번호] 순서)

	public int getLifeId() {
		return lifeId;
	}

	public void setLifeId(int lifeId) {
		this.lifeId = lifeId;
	}

	public String getStory() {
		return story;
	}

	public void setStory(String story) {
		this.story = story;
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
