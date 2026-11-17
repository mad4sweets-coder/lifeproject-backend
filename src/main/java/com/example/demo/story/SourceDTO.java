package com.example.demo.story;

// 출처 1개 (DATA_SOURCE)
public class SourceDTO {
	private int sourceId;	// 출처 번호
	private String title;	// 자료명
	private String url;	// URL

	public int getSourceId() {
		return sourceId;
	}

	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
