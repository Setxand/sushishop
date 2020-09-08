package com.sushishop.service;

import com.sushishop.client.EmailClient;
import com.sushishop.dto.AddressDTO;
import com.sushishop.dto.UserDTO;
import com.sushishop.model.Address;
import com.sushishop.model.User;
import com.sushishop.repository.AddressRepository;
import com.sushishop.repository.UserRepository;
import com.sushishop.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
public class UserService {

	private static final String INVALID_EMAIL = "Invalid User Email";
	private static final String INVALID_USER = "Invalid User ID";
	private static final String INVALID_LOGIN = "Invalid login";

	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;
	private final AddressRepository addressRepo;
	private final EmailClient emailClient;
	private final JwtTokenUtil jwtTokenUtil;
	private final String forgotPasswordUrl;

	public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder,
					   AddressRepository addressRepo,
					   EmailClient emailClient,
					   JwtTokenUtil jwtTokenUtil,
						@Value("${ui.forgotpass.url}") String forgotPasswordUrl) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
		this.addressRepo = addressRepo;
		this.emailClient = emailClient;
		this.jwtTokenUtil = jwtTokenUtil;
		this.forgotPasswordUrl = forgotPasswordUrl;
	}

	public User findByEmail(String email) {
		return userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(INVALID_EMAIL));
	}

	public User getUser(String userId) {
		return userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException(INVALID_USER));
	}

	@Transactional
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

		if (dto.role != null) {
			user.setRole(User.UserRole.valueOf(dto.role));
		}

		user.setName(dto.name);
		user.setPassword(passwordEncoder.encode(dto.password));

		User savedUser = userRepo.saveAndFlush(user);


		savedUser.setAddress(addressRepo.saveAndFlush(new Address(savedUser.getId())));

		return savedUser;
	}

	@Transactional
	public void addAddress(String userId, AddressDTO dto) {
		User user = getUser(userId);
		Address address = new Address();

		address.setId(userId);
		address.setStreet(dto.street);
		address.setRoomNumber(dto.roomNumber);
		address.setHousing(dto.housing);
		address.setHouse(dto.house);
		address.setFloor(dto.floor);
		address.setCity(dto.city);
		address.setEntrance(dto.entrance);

		Address savedAddress = addressRepo.saveAndFlush(address);
		user.setAddress(savedAddress);
	}

	public void forgotPassword(String email) {
		User user = userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid user Email"));

		String token = jwtTokenUtil.generateToken(user.getId(), user.getEmail(), JwtTokenUtil.TokenType.RESET_PASSWORD);
		String recoverPasswordUrl = forgotPasswordUrl + "?token=" + token;
		emailClient.sendResetPasswordEmail("Your url to change password: " + recoverPasswordUrl, email);
	}

	@Transactional
	public void changePassword(String userId, String password) {
		User user = getUser(userId);
		user.setPassword(passwordEncoder.encode(password));
	}

	@Transactional
	public User createAnonymous() {
		User user = new User();
		user.setRole(User.UserRole.ROLE_ANONYMOUS);
		user.setEmail(UUID.randomUUID().toString());
		user.setPhone(UUID.randomUUID().toString());
		return userRepo.saveAndFlush(user);
	}
}
