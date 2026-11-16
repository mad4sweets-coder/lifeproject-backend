package com.example.demo.member;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 실제 일(검사, 암호화, 저장)을 하는 곳
@Service
public class MemberService {

	private MemberDAO dao;
	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	public MemberService(MemberDAO dao) {
		this.dao = dao;
	}

	// ===== 중복 확인 (true = 사용 가능) =====
	public boolean isLoginIdAvailable(String loginId) {
		return dao.countByLoginId(loginId) == 0;
	}

	public boolean isNicknameAvailable(String nickname) {
		return dao.countByNickname(nickname) == 0;
	}

	public boolean isEmailAvailable(String email) {
		return dao.countByEmail(email) == 0;
	}

	// ===== 회원가입 (M01) =====
	// 문제가 있으면 오류 문구를 돌려주고, 성공하면 null 을 돌려준다
	@Transactional // 회원 INSERT 와 특성 INSERT 중 하나라도 실패하면 둘 다 취소
	public String join(MemberDTO dto) {

		// 1. 입력 형식 검사 (화면에서도 검사하지만 서버에서 한 번 더)
		if (dto.getLoginId() == null || !dto.getLoginId().matches("^[a-zA-Z0-9]{4,20}$")) {
			return "아이디는 영문·숫자 4~20자로 입력해 주세요.";
		}
		if (dto.getPassword() == null
				|| !dto.getPassword().matches("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,20}$")) {
			return "비밀번호는 영문·숫자·특수문자를 포함해 8~20자로 입력해 주세요.";
		}
		if (!dto.getPassword().equals(dto.getPasswordCheck())) {
			return "비밀번호 확인이 일치하지 않습니다.";
		}
		if (dto.getNickname() == null || dto.getNickname().length() < 2 || dto.getNickname().length() > 10) {
			return "닉네임은 2~10자로 입력해 주세요.";
		}
		if (dto.getEmail() == null || !dto.getEmail().matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
			return "이메일 형식이 올바르지 않습니다.";
		}

		// 2. 중복 검사
		if (!isLoginIdAvailable(dto.getLoginId())) return "이미 사용 중인 아이디입니다.";
		if (!isNicknameAvailable(dto.getNickname())) return "이미 사용 중인 닉네임입니다.";
		if (!isEmailAvailable(dto.getEmail())) return "이미 사용 중인 이메일입니다.";

		// 3. 비밀번호를 BCrypt 로 바꿔서 담기 (원래 비밀번호는 저장 안 함)
		dto.setPassword(encoder.encode(dto.getPassword()));

		// 4. MEMBER 에 1줄 저장 → dto.memberId 에 새 번호가 들어옴
		dao.insertMember(dto);

		// 5. 그 번호로 특성 4종 Lv.1 저장
		dao.insertDefaultTraits(dto.getMemberId());

		return null; // 성공
	}
}
