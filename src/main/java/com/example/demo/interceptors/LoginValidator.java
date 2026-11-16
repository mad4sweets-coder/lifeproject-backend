package com.example.demo.interceptors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.commons.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginValidator implements HandlerInterceptor {
	
	private  JwtUtil jwt;
	LoginValidator(JwtUtil jwt) {
		this.jwt = jwt;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		String uri = request.getRequestURI();
		String method = request.getMethod();
		
		
		if(request.getMethod().equalsIgnoreCase("OPTIONS")){
			return true;
		}
		
		if ( uri.equals("/members") && method.equalsIgnoreCase("POST")) {
			return true;
		}
		
		String authHeader = request.getHeader("Authorization");
		
		if(authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			
			DecodedJWT dToken = jwt.verifyToken(token);
			String id = dToken.getSubject();
			request.setAttribute("id", id);
			
			return true;
		}
		
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		
		return false;
	}
}
