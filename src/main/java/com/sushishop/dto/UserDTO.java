package com.sushishop.dto;

public class UserDTO {

	public String id;
	public String name;
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
