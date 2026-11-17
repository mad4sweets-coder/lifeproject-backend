package com.example.demo.story;

// 출처 1개 (DATA_SOURCE) - C19 이야기, C20 답변에서 사용
public class SourceDTO {
	private int no;			// 글 속 번호 ([1]이면 1)
	private int sourceId;	// 출처 번호
	private String title;	// 자료명
	private String url;		// URL

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

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
