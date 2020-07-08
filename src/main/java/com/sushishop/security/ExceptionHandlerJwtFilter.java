package com.sushishop.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sushishop.exception.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.catalina.filters.ExpiresFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

@Component
public class ExceptionHandlerJwtFilter extends OncePerRequestFilter {

	@Autowired ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (RuntimeException ex) {
			generateResponse(response, "ACCESS_DENIED", ex.getMessage(), SC_FORBIDDEN);
		}
	}

	private void generateResponse(HttpServletResponse response, String code, String message, int respCode)
			throws IOException {
		ErrorResponse errorResponse = new ErrorResponse(code, message);
		response.setStatus(respCode);
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
