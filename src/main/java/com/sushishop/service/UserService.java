package com.sushishop.service;

import com.sushishop.model.User;
import com.sushishop.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	private static final String INVALID_EMAIL = "Invalid User Email";
	private static final String INVALID_USER = "Invalid User ID";

	private final UserRepository userRepo;

	public UserService(UserRepository userRepo) {
		this.userRepo = userRepo;
	}

	public User findByEmail(String email) {
		return userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(INVALID_EMAIL));
	}

	public User getUser(String userId) {
		return userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException(INVALID_USER));
	}
}
