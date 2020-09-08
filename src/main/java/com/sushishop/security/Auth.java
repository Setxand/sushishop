package com.sushishop.security;

import com.sushishop.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

public class Auth {

	public static void isAdmin() {
		Collection<? extends GrantedAuthority> roles = getRoles();
		if (getRoles().stream().noneMatch(r -> r.getAuthority().equals(User.UserRole.ROLE_ADMIN.name()))) {
			throw new AccessDeniedException("Invalid role");
		}
	}

	private static Collection<? extends GrantedAuthority>  getRoles() {
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities();
	}
}
