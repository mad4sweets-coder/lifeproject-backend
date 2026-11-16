package com.example.demo.member;

import org.apache.ibatis.annotations.Mapper;

// DB에 SQL을 보내는 창구 (MyBatis)
// 메서드 이름 = member-mapper.xml 의 id 와 똑같아야 한다
@Mapper
public interface MemberDao {

	// 아이디 중복 확인 → 같은 아이디 개수 (0이면 사용 가능)
	int countByLoginId(String loginId);

	// 닉네임 중복 확인
	int countByNickname(String nickname);

	// 이메일 중복 확인
	int countByEmail(String email);

	// 회원 1줄 INSERT (실행 후 dto.memberId 에 새 번호가 채워짐)
	int insertMember(MemberDto dto);

	// 새 회원의 특성 4종(INVEST, DISEASE, LOVE, STUDY)을 Lv.1로 INSERT
	int insertDefaultTraits(int memberId);
}
