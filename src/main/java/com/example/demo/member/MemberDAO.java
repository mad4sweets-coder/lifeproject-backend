package com.example.demo.member;

import java.util.Map;

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

	// 이 회원의 진행 중 인생 개수 (0 또는 1)
	public int countPlayingLife(int memberId) {
		return mybatis.selectOne("member.countPlayingLife", memberId);
	}

	// 비회원 인생에 회원 번호 채우기 → 바뀐 줄 수 (0이면 전환된 인생 없음)
	public int convertGuestLife(int memberId, String guestToken) {
		return mybatis.update("member.convertGuestLife",
				Map.of("memberId", memberId, "guestToken", guestToken));
	}
}
