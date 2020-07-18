package com.sushishop.service;

import com.sushishop.dto.AddressDTO;
import com.sushishop.dto.UserDTO;
import com.sushishop.model.Address;
import com.sushishop.model.User;
import com.sushishop.repository.AddressRepository;
import com.sushishop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class UserService {

	private static final String INVALID_EMAIL = "Invalid User Email";
	private static final String INVALID_USER = "Invalid User ID";
	private static final String INVALID_LOGIN = "Invalid login";

	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;
	private final AddressRepository addressRepo;

	public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder, AddressRepository addressRepo) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
		this.addressRepo = addressRepo;
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
}
