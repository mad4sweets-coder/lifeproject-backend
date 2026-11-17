package com.example.demo.admin;

// A04 회원·비회원별 인생 완료율
public class CompletionDTO {
	private String startType;	// MEMBER, GUEST
	private int total;	// 전체 인생 수
	private int ended;	// 끝까지 산 인생
	private int abandoned;	// 중간에 나간 인생 (새로 태어나기로 포기)
	private int playing;	// 진행 중
	private double endedRate;	// 완료율 % (끝까지 산 ÷ 끝난 것 전체)
	private double abandonedRate;	// 중도 이탈률 %

	public String getStartType() {
		return startType;
	}

	public void setStartType(String startType) {
		this.startType = startType;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getEnded() {
		return ended;
	}

	public void setEnded(int ended) {
		this.ended = ended;
	}

	public int getAbandoned() {
		return abandoned;
	}

	public void setAbandoned(int abandoned) {
		this.abandoned = abandoned;
	}

	public int getPlaying() {
		return playing;
	}

	public void setPlaying(int playing) {
		this.playing = playing;
	}

	public double getEndedRate() {
		return endedRate;
	}

	public void setEndedRate(double endedRate) {
		this.endedRate = endedRate;
	}

	public double getAbandonedRate() {
		return abandonedRate;
	}

	public void setAbandonedRate(double abandonedRate) {
		this.abandonedRate = abandonedRate;
	}
}
