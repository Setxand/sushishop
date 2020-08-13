package com.sushishop.controller;

import com.sushishop.dto.AddressDTO;
import com.sushishop.dto.UserDTO;
import com.sushishop.service.UserService;
import com.sushishop.util.DtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class UserController {

	@Autowired UserService userService;

	@GetMapping("/v1/users/{userId}")
	public UserDTO getUser(@PathVariable String userId) {
		return DtoUtil.user(userService.getUser(userId));
	}

	@PutMapping("/v1/users/{userId}/addresses")
	public void addAddress(@PathVariable String userId, @RequestBody AddressDTO dto) {
		userService.addAddress(userId, dto);
	}


}
