package com.sushishop.controller;

import com.sushishop.dto.LoginRequestDTO;
import com.sushishop.dto.UserDTO;
import com.sushishop.model.User;
import com.sushishop.security.JwtTokenUtil;
import com.sushishop.security.dto.JwtResponse;
import com.sushishop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@CrossOrigin
public class AuthController {

	@Autowired UserService userService;
	@Autowired JwtTokenUtil jwtTokenUtil;
	@Autowired AuthenticationManager authenticationManager;

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public JwtResponse signUp(@Valid @RequestBody UserDTO dto, BindingResult result) {
		User user = userService.createUser(dto);
		return createJwtResponse(user);
	}

	@PostMapping("/forgot-password")
	public void forgotPassword(@RequestParam String email) {
		userService.forgotPassword(email);
	}

	@PostMapping("/login")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public JwtResponse authenticate(@RequestBody LoginRequestDTO dto) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.email, dto.password));
		User user = userService.findByEmail(dto.email);
		return createJwtResponse(user);
	}

	@GetMapping("/refresh-token")
	public JwtResponse refreshToken() {
		String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = userService.getUser(userId);
		return createJwtResponse(user);
	}

	private JwtResponse createJwtResponse(User user) {
		String accessToken = jwtTokenUtil.generateToken(user.getId(), user.getEmail(), JwtTokenUtil.TokenType.ACCESS);
		String refreshToken = jwtTokenUtil.generateToken(user.getId(), user.getEmail(), JwtTokenUtil.TokenType.REFRESH);
		return new JwtResponse(accessToken, refreshToken, user.getId());
	}
}
