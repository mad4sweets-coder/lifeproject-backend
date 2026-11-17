package com.example.demo.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.commons.JwtUtil;

@RestController
public class AdminController {

	private final AdminService adminService;
	private final JwtUtil jwtUtil;

	public AdminController(AdminService adminService, JwtUtil jwtUtil) {
		this.adminService = adminService;
		this.jwtUtil = jwtUtil;
	}

	// 로그인 토큰에서 회원 번호 꺼내기 (없거나 잘못된 토큰이면 null)
	private Integer loginMemberId(String auth) {
		if (auth == null || !auth.startsWith("Bearer ")) {
			return null;
		}
		try {
			String id = jwtUtil.verifyToken(auth.substring(7)).getSubject();
			return Integer.parseInt(id);
		} catch (Exception e) {
			return null;
		}
	}

	// A04 관리자 메인  GET /api/admin/dashboard  (관리자 로그인 토큰 필요)
	@GetMapping("/api/admin/dashboard")
	public ResponseEntity<?> dashboard(@RequestHeader(value = "Authorization", required = false) String auth) {
		// 화면의 관리자 메뉴는 localStorage 값으로만 숨기므로, 서버에서 DB 권한을 한 번 더 확인
		if (!adminService.isAdmin(loginMemberId(auth))) {
			return ResponseEntity.status(403).body("관리자만 볼 수 있어요.");
		}
		return ResponseEntity.ok(adminService.dashboard());
	}
}
