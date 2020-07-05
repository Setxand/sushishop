package com.sushishop.security;


import com.sushishop.model.User;
import com.sushishop.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JwtUserDetails implements UserDetailsService {

	private final UserService userService;
	private final JwtTokenUtil jwtTokenUtil;

	public JwtUserDetails(UserService userService, JwtTokenUtil jwtTokenUtil) {
		this.userService = userService;
		this.jwtTokenUtil = jwtTokenUtil;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		User user = userService.findByEmail(email);

		return org.springframework.security.core.userdetails.User.builder()
				.username(user.getEmail())
				.password(user.getPassword())
				.authorities(getUserAuthorities(user)).build();

	}

	public UserDetails createUserDetails(User user) {

		return org.springframework.security.core.userdetails.User.builder()
				.username(user.getEmail())
				.password(user.getPassword())
				.authorities(getUserAuthorities(user)).build();

	}

	private List<GrantedAuthority> getUserAuthorities(User user) {
		List<GrantedAuthority> result = new ArrayList<>();
		result.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
		return result;
	}
}
