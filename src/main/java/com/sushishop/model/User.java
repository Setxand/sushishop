package com.sushishop.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {

	public enum UserRole {
		ROLE_USER
	}

	@Id
	@GenericGenerator(name = "uuid", strategy = "uuid")
	@GeneratedValue(generator = "uuid")
	private String id;
	private String name;
	private String email;
	private String phone;
	private String password;

	@Enumerated(EnumType.STRING)
	private UserRole role;

}
