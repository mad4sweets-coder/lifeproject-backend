package com.example.demo.member;

// 내 특성 레벨 한 줄 (MEMBER_TRAIT + TRAIT)
// C01 퀘스트 설정 화면의 "특성 레벨", M08 특성 강화 화면에서 같이 씀
public class MemberTraitDTO {

	private String traitId;   // INVEST, DISEASE, LOVE, STUDY
	private String traitName; // 특성명 (DB 이름)
	private int traitLevel;   // 지금 레벨 (1~5)
	private int maxLevel;     // 최대 레벨 (5)
	private String description; // 설명 (예: 투자 관련 이벤트 성공 확률 보정)

	public String getTraitId() { return traitId; }
	public void setTraitId(String traitId) { this.traitId = traitId; }

	public String getTraitName() { return traitName; }
	public void setTraitName(String traitName) { this.traitName = traitName; }

	public int getTraitLevel() { return traitLevel; }
	public void setTraitLevel(int traitLevel) { this.traitLevel = traitLevel; }

	public int getMaxLevel() { return maxLevel; }
	public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
}
