package com.sushishop.controller;

import com.sushishop.dto.UserDTO;
import com.sushishop.service.UserService;
import com.sushishop.util.DtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

	@Autowired UserService userService;

	@GetMapping("/v1/users/{userId}")
	public UserDTO getUser(@PathVariable String userId) {
		return DtoUtil.user(userService.getUser(userId));
	}

}
