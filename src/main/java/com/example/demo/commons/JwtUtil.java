package com.example.demo.commons;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

@Component
public class JwtUtil {
	
	private Long expiration;
	private String secret;
	
	private JWTVerifier jwt;
	private Algorithm algo;
	
	public JwtUtil(
			@Value("${jwt.expiration}") Long expiration,
			@Value("${jwt.secret}") String secret) {
		this.expiration = expiration;
		this.secret = secret;
		
		this.algo = Algorithm.HMAC256(secret);
		this.jwt = JWT.require(algo).build();
	}

	public String createToken(String id,String role) {
		return JWT.create()
				.withSubject(id)
				.withClaim("role", role)
				.withIssuedAt(new Date())
				.withExpiresAt(new Date(System.currentTimeMillis() + expiration))
				.sign(this.algo);
	}
	
	public DecodedJWT verifyToken(String token) {
		return jwt.verify(token);
	}
}
