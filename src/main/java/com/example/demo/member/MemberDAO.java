package com.example.demo.member;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class MemberDAO {

	private final SqlSessionTemplate mybatis;

	// 스프링이 MyBatis 실행기(SqlSessionTemplate)를 넣어 줌
	public MemberDAO(SqlSessionTemplate mybatis) {
		this.mybatis = mybatis;
	}

	// 아이디 중복 확인 → 같은 아이디 개수 (0이면 사용 가능)
	public int countByLoginId(String loginId) {
		return mybatis.selectOne("member.countByLoginId", loginId);
	}

	// 닉네임 중복 확인
	public int countByNickname(String nickname) {
		return mybatis.selectOne("member.countByNickname", nickname);
	}

	// 이메일 중복 확인
	public int countByEmail(String email) {
		return mybatis.selectOne("member.countByEmail", email);
	}

	// 회원 1줄 INSERT (실행 후 dto.memberId 에 새 번호가 채워짐)
	public int insertMember(MemberDTO dto) {
		return mybatis.insert("member.insertMember", dto);
	}

	// 새 회원의 특성 4종(INVEST, DISEASE, LOVE, STUDY)을 Lv.1로 INSERT
	public int insertDefaultTraits(int memberId) {
		return mybatis.insert("member.insertDefaultTraits", memberId);
	}

	// 로그인: 아이디로 회원 1명 찾기 (없으면 null)
	public MemberDTO selectByLoginId(String loginId) {
		return mybatis.selectOne("member.selectByLoginId", loginId);
	}
}
