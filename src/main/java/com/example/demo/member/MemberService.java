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

	// ===== 로그인 (M02) =====
	// 아이디·비밀번호가 맞으면 회원 정보를, 틀리면 null 을 돌려준다
	public MemberDTO login(String loginId, String password) {

		// 0. 빈 값이면 바로 실패
		if (loginId == null || password == null) return null;

		// 1. 아이디로 회원 찾기
		MemberDTO member = dao.selectByLoginId(loginId);
		if (member == null) return null; // 없는 아이디

		// 2. 탈퇴한 회원은 로그인 불가
		if ("WITHDRAWN".equals(member.getStatus())) return null;

		// 3. 입력한 비밀번호를 DB의 BCrypt 값과 비교
		if (!encoder.matches(password, member.getPassword())) return null;

		return member; // 성공
	}

	// ===== 비회원 인생 → 회원 인생 전환 (M02) =====
	// 전환했으면 true, 안 했으면 false
	public boolean convertGuestLife(int memberId, String guestToken) {

		// 1. 비회원으로 하던 인생이 없으면 할 일 없음
		if (guestToken == null || guestToken.isBlank()) return false;

		// 2. 회원에게 이미 진행 중인 인생이 있으면 전환하지 않음
		//    (회원당 진행 중 인생 1개 규칙, 남은 비회원 인생은 7일 뒤 자동 삭제)
		if (dao.countPlayingLife(memberId) > 0) return false;

		// 3. 비회원 인생에 회원 번호 채우기
		return dao.convertGuestLife(memberId, guestToken) > 0;
	}

	// ===== 마이페이지 (M04) =====
	// 회원 정보 + 포인트 + 살아본 인생 수 + 퀘스트 달성 수 + 진행 중인 인생 (없으면 null)
	public MyPageDTO myPage(int memberId) {
		return dao.selectMyPage(memberId);
	}

	// ===== 개인정보 수정 (M05) =====
	// 문제가 있으면 오류 문구를, 성공하면 null 을 돌려준다
	public String update(int memberId, MemberUpdateDTO dto) {

		// 1. DB 에서 지금 내 정보 꺼내기 (비밀번호 비교용)
		MemberDTO me = dao.selectById(memberId);
		if (me == null || "WITHDRAWN".equals(me.getStatus())) return "회원 정보를 찾을 수 없습니다.";

		// 2. 현재 비밀번호 확인 (BCrypt 비교)
		if (dto.getCurrentPassword() == null || !encoder.matches(dto.getCurrentPassword(), me.getPassword())) {
			return "현재 비밀번호가 올바르지 않습니다.";
		}

		// 3. 닉네임·이메일 형식 검사 (회원가입과 같은 규칙)
		if (dto.getNickname() == null || dto.getNickname().length() < 2 || dto.getNickname().length() > 10) {
			return "닉네임은 2~10자로 입력해 주세요.";
		}
		if (dto.getEmail() == null || !dto.getEmail().matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
			return "이메일 형식이 올바르지 않습니다.";
		}

		// 4. 중복 검사 (나 자신은 빼고 셈 → 닉네임 그대로 두면 통과)
		if (dao.countNicknameExceptMe(memberId, dto.getNickname()) > 0) return "이미 사용 중인 닉네임입니다.";
		if (dao.countEmailExceptMe(memberId, dto.getEmail()) > 0) return "이미 사용 중인 이메일입니다.";

		// 5. 새 비밀번호: 비워 두면 안 바꿈(null), 입력했으면 검사 후 BCrypt 로 바꿔서 담기
		String newPw = dto.getNewPassword();
		if (newPw == null || newPw.isBlank()) {
			dto.setNewPassword(null);
		} else {
			if (!newPw.matches("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,20}$")) {
				return "새 비밀번호는 영문·숫자·특수문자를 포함해 8~20자로 입력해 주세요.";
			}
			if (!newPw.equals(dto.getNewPasswordCheck())) {
				return "새 비밀번호 확인이 일치하지 않습니다.";
			}
			dto.setNewPassword(encoder.encode(newPw));
		}

		// 6. 저장 (아이디는 바꾸지 않음)
		dto.setMemberId(memberId);
		dao.updateMember(dto);
		return null; // 성공
	}

	// ===== 회원 탈퇴 (M06) =====
	// 비밀번호가 맞으면 STATUS 를 'WITHDRAWN' 으로 바꾸고 true
	// (회원 줄은 지우지 않음 → 인생·추모관 글과 외래키 오류 방지)
	public boolean withdraw(int memberId, String password) {
		MemberDTO me = dao.selectById(memberId);
		if (me == null || "WITHDRAWN".equals(me.getStatus())) return false;
		if (password == null || !encoder.matches(password, me.getPassword())) return false;

		return dao.updateWithdrawn(memberId) > 0;
	}
}
