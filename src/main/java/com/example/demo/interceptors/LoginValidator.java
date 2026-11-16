package com.example.demo.interceptors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.commons.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 로그인한 회원만 들어갈 수 있는 주소 앞을 지키는 문지기
// 어떤 주소에 적용할지는 config/WebConfig.java 에서 정함
@Component
public class LoginValidator implements HandlerInterceptor {

	private JwtUtil jwt;

	LoginValidator(JwtUtil jwt) {
		this.jwt = jwt;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		// 1. 브라우저가 본 요청 전에 보내는 확인 요청(CORS)은 그냥 통과
		if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
			return true;
		}

		// 2. "Authorization: Bearer 토큰" 헤더 꺼내기
		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);

			try {
				// 3. 토큰 검사 (가짜이거나 만료되면 예외 발생)
				DecodedJWT dToken = jwt.verifyToken(token);

				// 4. 토큰 안의 회원 번호를 꺼내서 Controller 가 쓸 수 있게 담아 둠
				//    Controller 에서: @RequestAttribute("memberId") String memberId
				request.setAttribute("memberId", dToken.getSubject());
				return true;
			} catch (JWTVerificationException e) {
				// 토큰이 잘못됐거나 만료됨 → 아래 401 로
			}
		}

		// 5. 토큰이 없거나 잘못됨 → 401 (로그인 필요)
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		return false;
	}
}
