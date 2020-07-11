package com.sushishop.security;


import com.sushishop.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil implements Serializable {

	public enum TokenType {
		ACCESS,
		REFRESH
	}

	public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
	private static final long serialVersionUID = -2550185165626007488L;

	@Value("${jwt.secret}")
	private String secret;

	public String getUserIdFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	public String generateToken(String userId, String email, TokenType type) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("type", type.name());
		claims.put("email", email);
		return doGenerateToken(claims, userId);
	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = (String) getClaimFromToken(token, c -> c.get("email"));
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	public String getTokenType(String jwtToken) {
		return (String) getClaimFromToken(jwtToken, c -> c.get("type"));
	}

	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
	}

	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	private String doGenerateToken(Map<String, Object> claims, String subject) {
		int expirationDate = claims.get("type").equals(TokenType.ACCESS.name()) ? 900000 : 1800000;
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expirationDate))
				.signWith(SignatureAlgorithm.HS512, secret).compact();
	}
}
