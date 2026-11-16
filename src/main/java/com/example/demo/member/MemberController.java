package com.example.demo.member;

import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.commons.JwtUtil;

// React 가 보내는 요청을 처음 받는 곳
@RestController
@RequestMapping("/api/members")
public class MemberController {

	private MemberService service;
	private JwtUtil jwtUtil;

	public MemberController(MemberService service, JwtUtil jwtUtil) {
		this.service = service;
		this.jwtUtil = jwtUtil;
	}

	// 회원가입 (M01)  POST /api/members
	// 보내는 JSON: { loginId, password, passwordCheck, nickname, email }
	@PostMapping
	public ResponseEntity<?> join(@RequestBody MemberDTO dto) {
		try {
			String error = service.join(dto);
			if (error != null) {
				return ResponseEntity.badRequest().body(Map.of("message", error)); // 400
			}
			return ResponseEntity.status(201).body(Map.of("message", "회원가입 완료")); // 201
		} catch (DuplicateKeyException e) {
			// 거의 동시에 같은 아이디로 가입한 경우 → DB UNIQUE 제약이 막아 줌
			return ResponseEntity.status(409).body(Map.of("message", "이미 사용 중인 정보가 있습니다."));
		}
	}

	// 아이디 중복 확인  GET /api/members/check-id?loginId=abc
	@GetMapping("/check-id")
	public Map<String, Boolean> checkId(@RequestParam String loginId) {
		return Map.of("available", service.isLoginIdAvailable(loginId));
	}

	// 닉네임 중복 확인  GET /api/members/check-nickname?nickname=홍길동
	@GetMapping("/check-nickname")
	public Map<String, Boolean> checkNickname(@RequestParam String nickname) {
		return Map.of("available", service.isNicknameAvailable(nickname));
	}

	// 이메일 중복 확인  GET /api/members/check-email?email=a@b.com
	@GetMapping("/check-email")
	public Map<String, Boolean> checkEmail(@RequestParam String email) {
		return Map.of("available", service.isEmailAvailable(email));
	}

	// 로그인 (M02)  POST /api/members/login
	// 보내는 JSON: { loginId, password }
	// 돌려주는 JSON: { token, nickname, memberRole }
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody MemberDTO dto) {
		MemberDTO member = service.login(dto.getLoginId(), dto.getPassword());

		// 어느 쪽이 틀렸는지는 알려주지 않음 (M02 규칙)
		if (member == null) {
			return ResponseEntity.status(401)
					.body(Map.of("message", "아이디 또는 비밀번호가 올바르지 않습니다."));
		}

		// 토큰 안에는 회원 번호만 넣음 (비밀번호·이메일은 넣지 않음)
		String token = jwtUtil.createToken(String.valueOf(member.getMemberId()));

		return ResponseEntity.ok(Map.of(
				"token", token,
				"nickname", member.getNickname(),
				"memberRole", member.getMemberRole()));
	}
}
