package com.sushishop.security;


import com.sushishop.model.User;
import com.sushishop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class JwtRequestFilter extends GenericFilterBean {

	@Autowired private JwtTokenUtil jwtTokenUtil;
	@Autowired private JwtUserDetails jwtUserDetails;
	@Autowired private UserService userService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
						 FilterChain chain) throws IOException, ServletException {

		final String requestTokenHeader = ((HttpServletRequest) request).getHeader("Authorization");

		String userId = null;
		String jwtToken = null;

		if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ") && requestTokenHeader.length() > 7) {
			jwtToken = requestTokenHeader.substring(7);
			try {
				userId = jwtTokenUtil.getUserIdFromToken(jwtToken);

				String requestURI = ((HttpServletRequest) request).getRequestURI();

				String tokenType = jwtTokenUtil.getTokenType(jwtToken);

				if (requestURI.contains("/refresh-token") && !tokenType.equals(JwtTokenUtil.TokenType.REFRESH.name())) {
					throw new AccessDeniedException("This is not a refresh token");
				}

				if (requestURI.contains("/v1/users/passwords") &&
						!tokenType.equals(JwtTokenUtil.TokenType.RESET_PASSWORD.name())) {
					throw new AccessDeniedException("Access denied");
				}

			} catch (IllegalArgumentException e) {
				throw new AccessDeniedException("Unable to get JWT Token");
			}
		}

		createAuthentication(userId, jwtToken, (HttpServletRequest) request);
		chain.doFilter(request, response);
	}

	private void createAuthentication(String userId, String jwtToken, HttpServletRequest request) {
		if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			User user = userService.getUser(userId);
			UserDetails userDetails = this.jwtUserDetails.createUserDetails(user);

			if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
			UsernamePasswordAuthenticationToken authToken =
					new UsernamePasswordAuthenticationToken(userId, userDetails, userDetails.getAuthorities());

			authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}
	}
}
