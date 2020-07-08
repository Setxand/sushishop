package com.sushishop.service;

import com.sushishop.dto.UserDTO;
import com.sushishop.model.Cart;
import com.sushishop.model.User;
import com.sushishop.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	private static final String INVALID_EMAIL = "Invalid User Email";
	private static final String INVALID_USER = "Invalid User ID";
	private static final String INVALID_LOGIN = "Invalid login";

	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
	}

	public User findByEmail(String email) {
		return userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(INVALID_EMAIL));
	}

	public User getUser(String userId) {
		return userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException(INVALID_USER));
	}

	public User createUser(UserDTO dto) {
		User user = new User();

		if (dto.email == null && dto.phone == null) {
			throw new IllegalArgumentException(INVALID_LOGIN);
		}

		if (dto.email != null) {
			user.setEmail(dto.email);
		}

		if (dto.phone != null) {
			user.setPhone(dto.phone);
		}
		user.setName(dto.name);
		user.setPassword(passwordEncoder.encode(dto.password));


		return userRepo.saveAndFlush(user);
	}
}
