package com.sushishop.dto;

import com.sushishop.validation.ValidUser;

import javax.validation.constraints.Email;

@ValidUser
public class UserDTO extends BaseDTO {

	public String id;
	public String name;

	@Email
	public String email;
	public String password;
	public String phone;
	public String role;
	public AddressDTO address;


	@Override
	public String toString() {
		return "\nname: " + name +
				"\nemail: " + email +
				"\nphone: " + phone;
	}
}
