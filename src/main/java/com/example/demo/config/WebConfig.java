package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.demo.interceptors.LoginValidator;

// LoginValidator(문지기)를 어느 주소에 세울지 정하는 곳
// ※ 비회원도 쓰는 주소(engine, event 등)는 넣지 않음 → 로그인 없이도 게임 가능
@Configuration
public class WebConfig implements WebMvcConfigurer {

	private LoginValidator loginValidator;

	public WebConfig(LoginValidator loginValidator) {
		this.loginValidator = loginValidator;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(loginValidator)
				// 회원 전용 주소 (마이페이지·정보 수정·탈퇴 등은 /api/members/... 아래에 만들 예정)
				.addPathPatterns("/api/members/**")
				// 로그인 없이 써야 하는 주소는 빼기
				.excludePathPatterns(
						"/api/members",          // 회원가입
						"/api/members/login",    // 로그인
						"/api/members/check*",
						"/api/members/find/**"); // 아이디·닉네임·이메일 중복 확인
	}
}
